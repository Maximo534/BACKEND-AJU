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
import java.util.UUID;

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
    @Transactional(rollbackFor = Exception.class)
    public BuenaPractica registrar(BuenaPractica dominio, MultipartFile anexo, MultipartFile ppt, List<MultipartFile> fotos, MultipartFile video, String usuario) throws Exception {

        // 1. Generar ID
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-BP", siguiente, corte, anio));

        // 2. Guardar BD
        dominio.setUsuarioRegistro(usuario);
        BuenaPractica registrado = persistencePort.guardar(dominio);

        // 3. Subir Archivos
        boolean hayArchivos = (anexo != null && !anexo.isEmpty()) || (ppt != null && !ppt.isEmpty()) ||
                (video != null && !video.isEmpty()) || (fotos != null && !fotos.isEmpty());

        if (hayArchivos) {
            // ✅ Generamos UUID para la sesión FTP de este registro
            String sessionKey = UUID.randomUUID().toString();
            try {
                ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
                String id = registrado.getId();

                // Pasamos la sessionKey en lugar del usuario
                if (anexo != null && !anexo.isEmpty())
                    uploadFile(anexo, id, "ANEXO_BP", sessionKey, null); // null = nombre fijo (reemplazo)

                if (ppt != null && !ppt.isEmpty())
                    uploadFile(ppt, id, "PPT_BP", sessionKey, null);

                if (video != null && !video.isEmpty())
                    uploadFile(video, id, "VIDEO_BP", sessionKey, null);

                if (fotos != null) {
                    int i = 1;
                    for (MultipartFile f : fotos) {
                        if (!f.isEmpty()) {
                            // Sufijo manual para fotos múltiples
                            uploadFile(f, id, "FOTO_BP", sessionKey, "_foto_" + i);
                            i++;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error archivos BP", e);
                // Opcional: throw new Exception("Error al subir evidencias: " + e.getMessage());
            } finally {
                ftpPort.finalizarSession(sessionKey);
            }
        }
        return registrado;
    }

    @Override
    @Transactional(readOnly = true)
    public BuenaPractica buscarPorId(String id) throws Exception {
        return persistencePort.buscarPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuenaPractica actualizar(BuenaPractica dominio, String usuario) throws Exception {
        if (dominio.getId() == null) throw new Exception("ID obligatorio");
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    // --- AGREGAR ARCHIVO ---
    // ✅ CORRECCIÓN: Usamos el parámetro 'usuario' para Logs de Auditoría
    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipoArchivo, String usuario) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] a la BP ID [{}]", usuario, tipoArchivo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("Archivo vacío");

        BuenaPractica bp = persistencePort.buscarPorId(idEvento);
        if (bp == null) throw new Exception("ID no válido");

        // ✅ UUID para sesión FTP aislada
        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            // Si es FOTO, generamos timestamp para que no se chanque. Si es ANEXO/PPT, va null para reemplazar.
            String sufijo = tipoArchivo.contains("FOTO") ? "_" + System.currentTimeMillis() : null;

            uploadFile(archivo, idEvento, tipoArchivo, sessionKey, sufijo);

        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    // --- ELIMINAR ---
    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado");
        String ruta = archivo.getRuta() + "/" + archivo.getNombre();

        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(ruta);
        } catch (Exception e) {
            log.warn("Error borrando FTP: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    // --- DESCARGA GENÉRICA (Temporal File) ---
    @Override
    public RecursoArchivo descargarArchivoPorTipo(String idEvento, String tipoArchivo) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);
        Archivo encontrado = archivos.stream()
                .filter(a -> tipoArchivo.equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró archivo " + tipoArchivo));

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("bp_" + tipoArchivo + "_" + idEvento, ".tmp");

        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try (InputStream ftpStream = ftpPort.descargarArchivo(encontrado.getRuta() + "/" + encontrado.getNombre());
             java.io.OutputStream tempStream = java.nio.file.Files.newOutputStream(tempFile)) {
            ftpStream.transferTo(tempStream);
        } catch (Exception e) {
            java.nio.file.Files.deleteIfExists(tempFile);
            throw e;
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        InputStream autoDeleteStream = new java.io.FileInputStream(tempFile.toFile()) {
            @Override public void close() throws java.io.IOException {
                super.close();
                java.nio.file.Files.deleteIfExists(tempFile);
            }
        };

        return RecursoArchivo.builder()
                .stream(autoDeleteStream)
                .nombreFileName(encontrado.getNombre())
                .build();
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        return generarReportePort.generarFichaBuenaPractica(id);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    // =========================================================================
    // MÉTODO PRIVADO UNIFICADO ("Cerebro" de la subida BP)
    // =========================================================================
    // Nota: A diferencia de JI, aquí pasamos el ID y un sufijo manual
    // porque las reglas de nombrado son distintas (Reemplazo vs Agregado).
    private void uploadFile(MultipartFile file, String id, String tipo, String sessionKey, String sufijoManual) throws Exception {
        String anio = String.valueOf(LocalDate.now().getYear());
        String carpeta = "otros";
        String extPredeterminada = "";

        if (tipo.contains("ANEXO")) { carpeta = "anexos"; extPredeterminada = ".pdf"; }
        else if (tipo.contains("PPT")) { carpeta = "ppts"; extPredeterminada = ".pptx"; }
        else if (tipo.contains("FOTO")) { carpeta = "fotos"; extPredeterminada = ".jpg"; }
        else if (tipo.contains("VIDEO")) { carpeta = "videos"; extPredeterminada = ".mp4"; }

        // Ruta plana: /evidencias/bp/CARPETA/AÑO
        String rutaRelativa = String.format("%s/bp/%s/%s", ftpRutaBase, carpeta, anio);

        String ext = obtenerExtension(file.getOriginalFilename());
        if(ext.isEmpty()) ext = extPredeterminada;

        String nombreFinal;
        if (sufijoManual != null) {
            // Nombre dinámico (ej: ID_foto_1.jpg o ID_timestamp.jpg)
            nombreFinal = id + sufijoManual + ext;
        } else {
            // Nombre fijo para reemplazo (ej: ID_anexo.pdf)
            if (tipo.contains("ANEXO")) nombreFinal = id + "_anexo" + ext;
            else if (tipo.contains("PPT")) nombreFinal = id + "_presentacion" + ext;
            else if (tipo.contains("VIDEO")) nombreFinal = id + "_video" + ext;
            else nombreFinal = id + "_" + System.currentTimeMillis() + ext;
        }

        String rutaCompleta = rutaRelativa + "/" + nombreFinal;

        // Usamos la sessionKey (UUID) para subir
        if (!ftpPort.uploadFileFTP(sessionKey, rutaCompleta, file.getInputStream(), tipo)) {
            throw new Exception("Fallo FTP al subir " + nombreFinal);
        }

        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo)
                .ruta(rutaRelativa)
                .numeroIdentificacion(id)
                .build());
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }
}