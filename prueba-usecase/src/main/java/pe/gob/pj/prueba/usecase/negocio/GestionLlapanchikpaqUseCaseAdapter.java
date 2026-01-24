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
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.LlapanchikpaqPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionLlapanchikpaqUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionLlapanchikpaqUseCaseAdapter implements GestionLlapanchikpaqUseCasePort {

    private final LlapanchikpaqPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final GenerarReportePort reportePort;
    private final FtpPort ftpPort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    public Pagina<LlapanchikpaqJusticia> listar(String usuario, LlapanchikpaqJusticia filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    public LlapanchikpaqJusticia buscarPorId(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("ID obligatorio");
        return persistencePort.buscarPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlapanchikpaqJusticia registrar(LlapanchikpaqJusticia dominio, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception {
        if (dominio.getTareas() != null) {
            for (var tarea : dominio.getTareas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
        //Generar ID
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null && !ultimoId.isBlank()) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-LL", siguiente, corte, anio));

        // BD
        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        LlapanchikpaqJusticia registrado = persistencePort.guardar(dominio);

        //Archivos
        boolean hayArchivos = (anexo != null && !anexo.isEmpty()) || (fotos != null && !fotos.isEmpty());
        if (hayArchivos) {
            String sessionKey = UUID.randomUUID().toString();
            try {
                ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

                if (anexo != null && !anexo.isEmpty())
                    uploadFile(anexo, registrado, "ANEXO_LLJ", sessionKey);

                if (fotos != null) {
                    for (MultipartFile f : fotos) {
                        if (!f.isEmpty()) uploadFile(f, registrado, "FOTO_LLJ", sessionKey);
                    }
                }
            } catch (Exception e) {
                log.error("Error archivos LLJ", e);
                 throw new Exception("Error carga archivos: " + e.getMessage());
            } finally {
                ftpPort.finalizarSession(sessionKey);
            }
        }
        return registrado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlapanchikpaqJusticia actualizar(LlapanchikpaqJusticia dominio, String usuario) throws Exception {
        log.info("Actualizando LLJ ID: {} por: {}", dominio.getId(), usuario);
        if (dominio.getTareas() != null) {
            for (var tarea : dominio.getTareas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] al evento LLJ ID [{}]", usuario, tipo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("Archivo vacío");

        LlapanchikpaqJusticia evento = persistencePort.buscarPorId(idEvento);
        if (evento == null) throw new Exception("ID no válido");

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            uploadFile(archivo, evento, tipo, sessionKey);
        } catch (Exception e) {
            log.error("Error agregando archivo LLJ", e);
            throw new Exception("Error al subir: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String id, String tipoArchivo) throws Exception {
        // Buscar el archivo en BD
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);
        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().equalsIgnoreCase(tipoArchivo))
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró archivo " + tipoArchivo));

        // Crear archivo temporal
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("llj_" + tipoArchivo + "_" + id, ".tmp");
        String sessionKey = UUID.randomUUID().toString();

        // Iniciar sesión y descargar
        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

        try {
            String rutaCompleta = encontrado.getRuta() + "/" + encontrado.getNombre();
            InputStream ftpStream = ftpPort.downloadFileStream(sessionKey, rutaCompleta);

            if (ftpStream == null) {
                throw new Exception("El archivo no existe en el servidor FTP.");
            }

            try (java.io.OutputStream tempStream = java.nio.file.Files.newOutputStream(tempFile)) {
                ftpStream.transferTo(tempStream);
            }
            ftpStream.close();

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
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado en BD");

        String rutaCompleta = archivo.getRuta() + "/" + archivo.getNombre();
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(rutaCompleta);
        } catch (Exception e) {
            log.warn("Error borrado físico FTP: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        if(persistencePort.buscarPorId(id) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaLlj(id);
    }

    private void uploadFile(MultipartFile file, LlapanchikpaqJusticia evento, String tipo, String sessionKey) throws Exception {

        String carpeta = switch (tipo.toUpperCase()) {
            case "ANEXO_LLJ" -> "anexos";
            case "FOTO_LLJ" -> "fotos";
            case "VIDEO_LLJ" -> "videos";
            default -> "otros";
        };

        // Ruta plana por año: /evidencias/llj/CARPETA/ANIO
        String anio = String.valueOf(evento.getFechaInicio().getYear());
        String rutaRelativa = String.format("%s/llj/%s/%s", ftpRutaBase, carpeta, anio);

        String ext = obtenerExtension(file.getOriginalFilename());

        // Nombre: ID_UUID.ext para evitar colisiones
        String nombreFinal = evento.getId() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;

        if (!ftpPort.uploadFileFTP(sessionKey, rutaRelativa + "/" + nombreFinal, file.getInputStream(), "Carga " + tipo)) {
            throw new Exception("Fallo FTP al subir " + nombreFinal);
        }

        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo)
                .ruta(rutaRelativa)
                .numeroIdentificacion(evento.getId())
                .build());
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }
}