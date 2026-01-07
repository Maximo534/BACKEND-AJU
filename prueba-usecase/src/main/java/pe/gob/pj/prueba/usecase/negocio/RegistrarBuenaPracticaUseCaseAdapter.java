package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.BuenaPracticaPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarBuenaPracticaUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarBuenaPracticaUseCaseAdapter implements RegistrarBuenaPracticaUseCasePort {

    private final BuenaPracticaPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;
    private final GenerarReportePort generarReportePort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    public Pagina<BuenaPractica> listar(String usuario, BuenaPractica filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        return generarReportePort.generarFichaBuenaPractica(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BuenaPractica buscarPorId(String id) throws Exception {
        return persistencePort.buscarPorId(id);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    // ✅ MÉTODO REGISTRAR ACTUALIZADO
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuenaPractica registrar(BuenaPractica bp, MultipartFile anexo, MultipartFile ppt, List<MultipartFile> fotos, MultipartFile video, String usuario) throws Exception {
        log.info("Iniciando registro BP por: {}", usuario);

        // 1. GENERAR ID: 000001-15-2025-BP
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;

        if (ultimoId != null) {
            try {
                String parteNumerica = ultimoId.substring(0, 6);
                siguiente = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) { siguiente = 1; }
        }

        String numeroStr = String.format("%06d", siguiente);
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (bp.getDistritoJudicialId() != null) ? bp.getDistritoJudicialId() : "00";
        String sufijo = "BP";

        String idGenerado = String.format("%s-%s-%s-%s", numeroStr, corte, anio, sufijo);
        if(idGenerado.length() > 17) idGenerado = idGenerado.substring(0, 17);

        bp.setId(idGenerado);
        bp.setUsuarioRegistro(usuario);

        // 2. GUARDAR DATOS EN BD
        BuenaPractica registrado = persistencePort.guardar(bp);

        // 3. SUBIR ARCHIVOS AL FTP
        try {
            ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            // A. ANEXO (PDF)
            // (Mantenemos tu lógica original para el anexo)
            if (anexo != null && !anexo.isEmpty()) {
                String ruta = String.format("%s/bp/anexos/%s/%s.pdf", ftpRutaBase, anio, idGenerado);
                ftpPort.uploadFileFTP(usuario, ruta, anexo.getInputStream(), "Anexo BP");
                archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                        .nombre(idGenerado + "_anexo.pdf").tipo("ANEXO_BP").ruta(ruta).numeroIdentificacion(idGenerado).build());
            }

            // B. PPT (PowerPoint) - ✅ NUEVO
            if (ppt != null && !ppt.isEmpty()) {
                String ext = obtenerExtension(ppt.getOriginalFilename());
                if (ext.isEmpty()) ext = ".pptx"; // Default por seguridad

                // Usamos el helper para mantener el código limpio
                subirArchivoUnitario(ppt, idGenerado, anio, "PPT_BP", "ppts", ext, usuario);
            }

            // C. FOTOS (Lista)
            if (fotos != null) {
                int i = 1;
                for (MultipartFile f : fotos) {
                    if (!f.isEmpty()) {
                        String nombre = idGenerado + "_foto_" + i + ".jpg";
                        String ruta = String.format("%s/bp/fotos/%s/%s", ftpRutaBase, anio, nombre);
                        ftpPort.uploadFileFTP(usuario, ruta, f.getInputStream(), "Foto BP " + i);
                        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                                .nombre(nombre).tipo("FOTO_BP").ruta(ruta).numeroIdentificacion(idGenerado).build());
                        i++;
                    }
                }
            }

            // D. VIDEO (MP4)
            if (video != null && !video.isEmpty()) {
                String ruta = String.format("%s/bp/videos/%s/%s.mp4", ftpRutaBase, anio, idGenerado);
                ftpPort.uploadFileFTP(usuario, ruta, video.getInputStream(), "Video BP");
                archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                        .nombre(idGenerado + "_video.mp4").tipo("VIDEO_BP").ruta(ruta).numeroIdentificacion(idGenerado).build());
            }

        } catch (Exception e) {
            log.error("Error archivos BP", e);
            throw new Exception("Error subiendo evidencias: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuario);
        }

        return registrado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuenaPractica actualizar(BuenaPractica bp, String usuario) throws Exception {
        // 1. Buscamos el original
        BuenaPractica original = persistencePort.buscarPorId(bp.getId());
        if (original == null) throw new Exception("No existe la Buena Práctica con ID: " + bp.getId());

        // 2. Mapeamos SOLO los campos que vienen del Frontend
        original.setTitulo(bp.getTitulo());
        original.setDistritoJudicialId(bp.getDistritoJudicialId());
        original.setResponsable(bp.getResponsable());
        original.setEmail(bp.getEmail());
        original.setTelefono(bp.getTelefono());
        original.setIntegrantes(bp.getIntegrantes());
        original.setFechaInicio(bp.getFechaInicio());
        original.setCategoria(bp.getCategoria());

        original.setProblema(bp.getProblema());
        original.setCausa(bp.getCausa());
        original.setConsecuencia(bp.getConsecuencia());
        original.setDescripcionGeneral(bp.getDescripcionGeneral());
        original.setLogro(bp.getLogro());
        original.setObjetivo(bp.getObjetivo());
        original.setAliado(bp.getAliado());
        original.setDificultad(bp.getDificultad());
        original.setNorma(bp.getNorma());
        original.setDesarrollo(bp.getDesarrollo());
        original.setEjecucion(bp.getEjecucion());
        original.setActividad(bp.getActividad());
        original.setAporte(bp.getAporte());
        original.setResultado(bp.getResultado());
        original.setImpacto(bp.getImpacto());
        original.setPublicoObjetivo(bp.getPublicoObjetivo());
        original.setLeccionAprendida(bp.getLeccionAprendida());
        original.setInfoAdicional(bp.getInfoAdicional());

        // ❌ IMPORTANTE: NO hacemos set de 'aporteRelevante', 'situacionAnterior', etc.
        // Al ignorarlos aquí, JPA mantiene el valor que ya estaba en la base de datos.

        original.setUsuarioRegistro(usuario);

        // 3. Guardar (Aquí se activará el @PreUpdate si algo quedó null)
        return persistencePort.guardar(original);
    }
    // --- 2. ELIMINAR ARCHIVO (Reutilizado de JI) ---
    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        log.info("Eliminando archivo: {}", nombreArchivo);

        // 1. Buscamos metadata en BD
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado en BD");

        // 2. Eliminamos físico del FTP
        String rutaCompletaFtp = archivo.getRuta(); // Ojo: verifica si tu ruta en BD incluye el nombre o es solo carpeta
        // Si en BD guardas solo la carpeta, usa: archivo.getRuta() + "/" + archivo.getNombre();

        ftpPort.iniciarSesion("ELIMINAR", ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(rutaCompletaFtp);
        } catch (Exception e) {
            log.warn("No se pudo borrar del FTP (quizás ya no existe), pero seguimos con BD: {}", e.getMessage());
        } finally {
            ftpPort.finalizarSession("ELIMINAR");
        }

        // 3. Eliminamos referencia en BD
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    // --- 3. AGREGAR ARCHIVO ADICIONAL (Desde botón "+" o upload) ---
    @Override
    @Transactional
    public void subirArchivoAdicional(String idEvento, MultipartFile archivo, String tipoArchivo, String usuario) throws Exception {
        log.info("Subiendo archivo adicional ({}) al evento {}", tipoArchivo, idEvento);

        BuenaPractica bp = persistencePort.buscarPorId(idEvento);
        if (bp == null) throw new Exception("ID no válido");

        String anio = String.valueOf(LocalDate.now().getYear()); // O el año del evento: String.valueOf(bp.getFechaInicio().getYear());

        // Determinamos carpeta y extensión según el tipo
        String carpeta = "otros";
        String ext = obtenerExtension(archivo.getOriginalFilename());
        String tipoBD = tipoArchivo.toUpperCase(); // ANEXO_BP, PPT_BP, FOTO_BP

        if (tipoBD.contains("ANEXO")) { carpeta = "anexos"; ext = ".pdf"; }
        else if (tipoBD.contains("PPT")) { carpeta = "ppts"; if(ext.isEmpty()) ext = ".pptx"; }
        else if (tipoBD.contains("FOTO")) { carpeta = "fotos"; if(ext.isEmpty()) ext = ".jpg"; }
        else if (tipoBD.contains("VIDEO")) { carpeta = "videos"; if(ext.isEmpty()) ext = ".mp4"; }

        // Reutilizamos el helper que creamos antes
        ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            // Nota: Si es ANEXO o PPT, quizás quieras borrar el anterior primero si solo se permite 1.
            // O el Front se encarga de llamar a "eliminar" antes de "subir".

            // Usamos un nombre único para fotos/videos, o fijo para Anexos si quieres reemplazar
            String nombreFinal;
            if (tipoBD.contains("ANEXO") || tipoBD.contains("PPT")) {
                nombreFinal = idEvento + "_" + carpeta + ext; // Reemplaza el existente
            } else {
                // Para fotos/videos generamos ID único para no chancar las otras
                nombreFinal = idEvento + "_" + System.currentTimeMillis() + ext;
            }

            String ruta = String.format("%s/bp/%s/%s/%s", ftpRutaBase, carpeta, anio, nombreFinal);

            ftpPort.uploadFileFTP(usuario, ruta, archivo.getInputStream(), "Add " + tipoBD);

            // Guardar/Actualizar en BD
            // Si es Anexo/PPT, primero verificamos si ya existe para actualizar ruta o borrar anterior
            // Simplificación: Guardamos referencia nueva.
            archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                    .nombre(nombreFinal) // IMPORTANTE: Este nombre se usa para borrar después
                    .tipo(tipoBD)
                    .ruta(ruta)
                    .numeroIdentificacion(idEvento)
                    .build());

        } finally {
            ftpPort.finalizarSession(usuario);
        }
    }

    // ✅ MÉTODO PARA DESCARGAS (Usado por /documento y /ppt en el Controller)
    @Override
    public RecursoArchivo descargarArchivoPorTipo(String idEvento, String tipoArchivo) throws Exception {
        // 1. Buscamos archivos en BD
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);

        // 2. Filtramos el tipo deseado (ANEXO_BP o PPT_BP)
        Archivo encontrado = archivos.stream()
                .filter(a -> tipoArchivo.equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró archivo de tipo " + tipoArchivo));

        // 3. Descargamos del FTP
        ftpPort.iniciarSesion("DESC_FILE", ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            InputStream stream = ftpPort.descargarArchivo(encontrado.getRuta());
            return RecursoArchivo.builder()
                    .stream(stream)
                    .nombreFileName(encontrado.getNombre()) // Devolvemos el nombre real del archivo
                    .build();
        } catch (Exception e) {
            ftpPort.finalizarSession("DESC_FILE");
            throw e;
        }
    }

    // --- HELPERS PRIVADOS ---

    // Este helper lo usamos para el PPT (y podrías usarlo para el Anexo si quisieras refactorizar)
    private void subirArchivoUnitario(MultipartFile file, String id, String anio, String tipoBD, String carpetaFtp, String ext, String usuario) throws Exception {
        // Nombre del archivo: ID_ppts.pptx
        String nombre = id + "_" + carpetaFtp + ext;

        // Ruta FTP: /evidencias/bp/ppts/2025/ID_ppts.pptx
        String ruta = String.format("%s/bp/%s/%s/%s", ftpRutaBase, carpetaFtp, anio, nombre);

        // Subida al FTP
        ftpPort.uploadFileFTP(usuario, ruta, file.getInputStream(), "Carga " + tipoBD);

        // Guardado en BD
        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombre)
                .tipo(tipoBD)
                .ruta(ruta)
                .numeroIdentificacion(id)
                .build());
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }
}