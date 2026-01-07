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
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JuezPazEscolarPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionJuecesEscolaresUseCaseAdapter implements GestionJuecesEscolaresUseCasePort {

    private final JuezPazEscolarPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;
    private final GenerarReportePort reportePort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    // ==========================================
    // SECCIÓN 1: GESTIÓN DE JUECES (ALUMNOS)
    // ==========================================

    @Override
    @Transactional
    public JuezPazEscolar registrarJuez(JuezPazEscolar juez, MultipartFile foto, MultipartFile resolucion, String usuarioOperacion) throws Exception {

        // 1. Validar duplicados
        if (persistencePort.existeDniEnColegio(juez.getDni(), juez.getInstitucionEducativaId())) {
            throw new Exception("El alumno con DNI " + juez.getDni() + " ya está registrado en este colegio.");
        }

        // 2. Completar Datos y Generar ID
        String uuid = UUID.randomUUID().toString().toUpperCase();
        juez.setId(uuid);
        juez.setFechaRegistro(LocalDate.now());
        juez.setUsuarioRegistro(usuarioOperacion);
        juez.setActivo("1");

        // 3. Guardar Entidad
        JuezPazEscolar registrado = persistencePort.guardar(juez);

        // 4. Subir Archivos
        try {
            ftpPort.iniciarSesion(usuarioOperacion, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            String anio = String.valueOf(LocalDate.now().getYear());

            // --- FOTO ALUMNO ---
            if (foto != null && !foto.isEmpty()) {
                subirArchivoUnitario(uuid, foto, "FOTO_JPE", "jpe/fotos/" + anio, usuarioOperacion);
            }

            // --- RESOLUCIÓN ---
            if (resolucion != null && !resolucion.isEmpty()) {
                subirArchivoUnitario(uuid, resolucion, "RESOLUCION_JPE", "jpe/resoluciones/" + anio, usuarioOperacion);
            }

        } catch (Exception e) {
            log.error("Error subiendo archivos JPE", e);
            throw new Exception("Se guardó el juez pero falló la carga de archivos: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuarioOperacion);
        }

        return registrado;
    }

    @Override
    public List<JuezPazEscolar> listarJuecesPorColegio(String colegioId) {
        return persistencePort.listarPorColegio(colegioId);
    }

    // ==========================================
    // SECCIÓN 2: GESTIÓN DE CASOS (INCIDENTES)
    // ==========================================

    @Override
    public Pagina<JpeCasoAtendido> listarCasos(JpeCasoAtendido filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listarCasos(filtros, pagina, tamanio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JpeCasoAtendido registrarCaso(JpeCasoAtendido caso, MultipartFile acta, List<MultipartFile> fotos, String usuario) throws Exception {
        log.info("Iniciando registro Caso JPE por: {}", usuario);

        // =================================================================================
        // 1. GENERAR ID: 000001-15-2025-PE (Igual que Buenas Prácticas)
        // =================================================================================
        String ultimoId = persistencePort.obtenerUltimoIdCaso();
        long siguiente = 1;

        if (ultimoId != null) {
            try {
                // Asumimos formato: 000001-XX-YYYY-PE
                // Tomamos los primeros 6 dígitos
                String parteNumerica = ultimoId.substring(0, 6);
                siguiente = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) {
                siguiente = 1; // Si falla el parseo, reiniciamos
            }
        }

        String numeroStr = String.format("%06d", siguiente);
        String anio = String.valueOf(LocalDate.now().getYear());

        // Obtenemos el ID de la corte (si viene nulo ponemos "00")
        String corte = (caso.getDistritoJudicialId() != null) ? caso.getDistritoJudicialId() : "00";
        String sufijo = "PE"; // PE = Paz Escolar

        // ID FINAL
        String idGenerado = String.format("%s-%s-%s-%s", numeroStr, corte, anio, sufijo);

        // Recorte de seguridad por si excede el tamaño de BD (CHAR 17)
        if(idGenerado.length() > 17) idGenerado = idGenerado.substring(0, 17);

        caso.setId(idGenerado);
        caso.setUsuarioRegistro(usuario);
        if(caso.getFechaRegistro() == null) caso.setFechaRegistro(LocalDate.now());

        // =================================================================================
        // 2. GUARDAR DATOS EN BD
        // =================================================================================
        JpeCasoAtendido registrado = persistencePort.guardarCaso(caso);

        // =================================================================================
        // 3. SUBIR ARCHIVOS AL FTP
        // =================================================================================
        try {
            ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            // A. ACTA (Equivalente al Anexo en BP)
            if (acta != null && !acta.isEmpty()) {
                String ruta = String.format("%s/jpe/actas/%s/%s.pdf", ftpRutaBase, anio, idGenerado);

                ftpPort.uploadFileFTP(usuario, ruta, acta.getInputStream(), "Acta JPE");

                archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                        .nombre(idGenerado + "_acta.pdf")
                        .tipo("ANEXO_JPE") // Usamos ANEXO para mantener estandar
                        .ruta(ruta)
                        .numeroIdentificacion(idGenerado)
                        .build());
            }

            // B. FOTOS (Lista)
            if (fotos != null) {
                int i = 1;
                for (MultipartFile f : fotos) {
                    if (!f.isEmpty()) {
                        String nombreFoto = idGenerado + "_foto_" + i + ".jpg";
                        String ruta = String.format("%s/jpe/fotos_casos/%s/%s", ftpRutaBase, anio, nombreFoto);

                        ftpPort.uploadFileFTP(usuario, ruta, f.getInputStream(), "Foto Caso " + i);

                        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                                .nombre(nombreFoto)
                                .tipo("FOTO_CASO_JPE")
                                .ruta(ruta)
                                .numeroIdentificacion(idGenerado)
                                .build());
                        i++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error archivos JPE", e);
            throw new Exception("Se guardó el caso " + idGenerado + " pero fallaron los archivos: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuario);
        }

        return registrado;
    }

    @Override
    @Transactional
    public JpeCasoAtendido actualizarCaso(JpeCasoAtendido caso, String usuario) throws Exception {

        JpeCasoAtendido original = persistencePort.buscarCasoPorId(caso.getId());
        if (original == null) throw new Exception("No existe el caso con ID: " + caso.getId());

        // Actualizamos campos editables
        original.setResumenHechos(caso.getResumenHechos());
        original.setAcuerdos(caso.getAcuerdos());
        original.setLugarActividad(caso.getLugarActividad());

        // Estudiante 1
        original.setNombreEstudiante1(caso.getNombreEstudiante1());
        original.setDniEstudiante1(caso.getDniEstudiante1());
        original.setGradoEstudiante1(caso.getGradoEstudiante1());
        original.setSeccionEstudiante1(caso.getSeccionEstudiante1());

        // Estudiante 2
        original.setNombreEstudiante2(caso.getNombreEstudiante2());
        original.setDniEstudiante2(caso.getDniEstudiante2());
        original.setGradoEstudiante2(caso.getGradoEstudiante2());
        original.setSeccionEstudiante2(caso.getSeccionEstudiante2());

        original.setUsuarioRegistro(usuario); // Auditoría

        return persistencePort.guardarCaso(original);
    }

    // ==========================================
    // SECCIÓN 3: GESTIÓN DE ARCHIVOS (Común)
    // ==========================================

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        log.info("Eliminando archivo JPE: {}", nombreArchivo);
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado en BD");

        // Borrar del FTP
        ftpPort.iniciarSesion("ELIMINAR", ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(archivo.getRuta());
        } catch (Exception e) {
            log.warn("No se pudo borrar del FTP: {}", e.getMessage());
        } finally {
            ftpPort.finalizarSession("ELIMINAR");
        }

        // Borrar de BD
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    @Transactional
    public void subirArchivoAdicional(String idCaso, MultipartFile archivo, String tipoArchivo, String usuario) throws Exception {
        JpeCasoAtendido caso = persistencePort.buscarCasoPorId(idCaso);
        if (caso == null) throw new Exception("ID Caso no válido");

        ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            String anio = String.valueOf(LocalDate.now().getYear());
            String carpeta = "jpe/otros";
            String ext = obtenerExtension(archivo.getOriginalFilename());

            // Definir carpeta según tipo
            if (tipoArchivo.contains("ANEXO")) carpeta = "jpe/actas/" + anio;
            else if (tipoArchivo.contains("FOTO")) carpeta = "jpe/fotos_casos/" + anio;

            // Generar nombre
            String nombreFinal;
            if (tipoArchivo.contains("ANEXO")) {
                nombreFinal = idCaso + "_acta" + ext; // Reemplaza acta
            } else {
                nombreFinal = idCaso + "_" + System.currentTimeMillis() + ext; // Nueva foto
            }

            String rutaCompleta = String.format("%s/%s/%s", ftpRutaBase, carpeta, nombreFinal);

            ftpPort.uploadFileFTP(usuario, rutaCompleta, archivo.getInputStream(), "Add " + tipoArchivo);

            archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                    .nombre(nombreFinal)
                    .tipo(tipoArchivo)
                    .ruta(rutaCompleta)
                    .numeroIdentificacion(idCaso)
                    .build());

        } finally {
            ftpPort.finalizarSession(usuario);
        }
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String id, String tipoArchivo) throws Exception {
        // Buscamos todos los archivos del ID
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);

        // Filtramos por tipo (ej: ANEXO_JPE) y tomamos el primero
        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().equalsIgnoreCase(tipoArchivo))
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró archivo tipo " + tipoArchivo + " para el ID " + id));

        ftpPort.iniciarSesion("DESCARGA", ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            InputStream stream = ftpPort.descargarArchivo(encontrado.getRuta());
            return RecursoArchivo.builder()
                    .nombreFileName(encontrado.getNombre())
                    .stream(stream)
                    .build();
        } finally {
            ftpPort.finalizarSession("DESCARGA");
        }
    }

    // --- HELPER PRIVADO ---
    private void subirArchivoUnitario(String id, MultipartFile file, String tipo, String subCarpeta, String usuario) throws Exception {
        String ext = obtenerExtension(file.getOriginalFilename());
        String sufijo = tipo.contains("FOTO") ? "foto" : (tipo.contains("ACTA") || tipo.contains("ANEXO") ? "acta" : "doc");
        String nombreFinal = id + "_" + sufijo + ext;

        String rutaCompleta = String.format("%s/%s/%s", ftpRutaBase, subCarpeta, nombreFinal);

        ftpPort.uploadFileFTP(usuario, rutaCompleta, file.getInputStream(), tipo);

        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo)
                .ruta(rutaCompleta)
                .numeroIdentificacion(id)
                .build());
    }

    private String obtenerExtension(String nombre) {
        if (nombre != null && nombre.contains(".")) return nombre.substring(nombre.lastIndexOf("."));
        return ".dat";
    }

    @Override
    public JpeCasoAtendido buscarCasoPorId(String id) throws Exception {
        JpeCasoAtendido caso = persistencePort.buscarCasoPorId(id);

        if (caso == null) {
            throw new Exception("Caso no encontrado con ID: " + id);
        }
        return caso;
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        return reportePort.generarFichaJpe(id);
    }
    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }
}