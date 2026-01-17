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
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaPazPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJusticiaPazUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionJusticiaPazUseCaseAdapter implements GestionJusticiaPazUseCasePort {

    private final JusticiaPazPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;
    private final GenerarReportePort reportePort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    // =========================================================================
    // SECCIÓN 2: GESTIÓN DE CASOS (INCIDENTES)
    // =========================================================================

    @Override
    public Pagina<JpeCasoAtendido> listar(String usuario, JpeCasoAtendido filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JpeCasoAtendido registrar(JpeCasoAtendido dominio, MultipartFile acta, List<MultipartFile> fotos, String usuario) throws Exception {

        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-PE", siguiente, corte, anio));

        dominio.setUsuarioRegistro(usuario);
        JpeCasoAtendido registrado = persistencePort.guardar(dominio);

        // Subida de Archivos Caso (Lógica Específica: Nombre único con timestamp)
        boolean hayArchivos = (acta != null && !acta.isEmpty()) || (fotos != null && !fotos.isEmpty());
        if (hayArchivos) {
            String sessionKey = UUID.randomUUID().toString();
            try {
                ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

                if (acta != null && !acta.isEmpty()) {
                    uploadFile(acta, registrado, "ACTA_JPE", sessionKey);
                }

                if (fotos != null) {
                    for (MultipartFile f : fotos) {
                        if (!f.isEmpty()) uploadFile(f, registrado, "FOTO_JPE", sessionKey);
                    }
                }
            } catch (Exception e) {
                log.error("Error archivos Caso JPE", e);
            } finally {
                ftpPort.finalizarSession(sessionKey);
            }
        }
        return registrado;
    }

    @Override
    @Transactional
    public JpeCasoAtendido actualizar(JpeCasoAtendido dominio, String usuario) throws Exception {
        if (dominio.getId() == null) throw new Exception("ID obligatorio");
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idCaso, MultipartFile archivo, String tipo, String usuario) throws Exception {
        JpeCasoAtendido caso = persistencePort.buscarPorId(idCaso);
        if (caso == null) throw new Exception("Caso no encontrado");

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            // ✅ Usa la lógica de Caso
            uploadFile(archivo, caso, tipo, sessionKey);
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String idCaso, String tipoArchivo) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idCaso);
        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().equalsIgnoreCase(tipoArchivo))
                .findFirst()
                .orElseThrow(() -> new Exception("Archivo no encontrado: " + tipoArchivo));

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("jpe_" + tipoArchivo + "_" + idCaso, ".tmp");
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
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no existe");

        String sessionKey = UUID.randomUUID().toString();
        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(archivo.getRuta() + "/" + archivo.getNombre());
        } catch (Exception e) {
            log.warn("Error borrado físico FTP: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public JpeCasoAtendido buscarPorId(String id) throws Exception {
        return persistencePort.buscarPorId(id);
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        return reportePort.generarFichaJpe(id);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }
    private void uploadFile(MultipartFile file, JpeCasoAtendido caso, String tipo, String sessionKey) throws Exception {
        String carpeta = switch (tipo.toUpperCase()) {
            case "ACTA_JPE" -> "actas";
            case "FOTO_JPE" -> "fotos";
            default -> "otros";
        };

        String anio = String.valueOf(caso.getFechaRegistro() != null ? caso.getFechaRegistro().getYear() : LocalDate.now().getYear());
        // Ruta: /evidencias/jpe/casos/CARPETA/ANIO
        String rutaRelativa = String.format("%s/jpe/casos/%s/%s", ftpRutaBase, carpeta, anio);

        String ext = obtenerExtension(file.getOriginalFilename());
        // Nombre único
        String nombreFinal = caso.getId() + "_" + tipo + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0,4) + ext;

        if (!ftpPort.uploadFileFTP(sessionKey, rutaRelativa + "/" + nombreFinal, file.getInputStream(), tipo)) {
            throw new Exception("Fallo FTP Caso");
        }

        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal).tipo(tipo)
                .ruta(rutaRelativa)
                .numeroIdentificacion(caso.getId())
                .build());
    }
    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }
}
