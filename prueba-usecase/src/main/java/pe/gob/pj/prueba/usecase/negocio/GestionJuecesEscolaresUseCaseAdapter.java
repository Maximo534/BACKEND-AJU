package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils; // Si tienes commons-lang, si no usamos lógica nativa
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JuezPazEscolarPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionJuecesEscolaresUseCaseAdapter implements GestionJuecesEscolaresUseCasePort {

    private final JuezPazEscolarPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    public Pagina<JuezPazEscolar> listar(JuezPazEscolar filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(filtros, pagina, tamanio);
    }

    @Override
    public JuezPazEscolar buscarPorId(String id) throws Exception {
        return persistencePort.buscarPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JuezPazEscolar registrar(JuezPazEscolar juez, MultipartFile resolucion, String usuario) throws Exception {
        if (persistencePort.existeDniEnColegio(juez.getDni(), juez.getInstitucionEducativaId())) {
            throw new Exception("El alumno con DNI " + juez.getDni() + " ya está registrado en este colegio.");
        }

        String idCorto = generarIdCorto();

        juez.setId(idCorto);
        juez.setFechaRegistro(LocalDate.now());
        juez.setUsuarioRegistro(usuario);
        juez.setActivo("1");

        JuezPazEscolar registrado = persistencePort.guardar(juez);

        if (resolucion != null && !resolucion.isEmpty()) {
            String sessionKey = UUID.randomUUID().toString();
            try {
                ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
                uploadFile(resolucion, registrado, "RESOLUCION_JPE", sessionKey);
            } catch (Exception e) {
                log.error("Error subiendo archivo", e);
            } finally {
                ftpPort.finalizarSession(sessionKey);
            }
        }
        return registrado;
    }

    @Override
    @Transactional
    public JuezPazEscolar actualizar(JuezPazEscolar juez, String usuario) throws Exception {
        if (juez.getId() == null) throw new Exception("ID Requerido");
        juez.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(juez);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idJuez, MultipartFile archivo, String tipo, String usuario) throws Exception {
        JuezPazEscolar juez = persistencePort.buscarPorId(idJuez);
        if (juez == null) throw new Exception("Juez no encontrado");

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            uploadFile(archivo, juez, tipo, sessionKey);
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado");

        String sessionKey = UUID.randomUUID().toString();
        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(archivo.getRuta() + "/" + archivo.getNombre());
        } catch (Exception e) {
            log.warn("Fallo borrado físico FTP: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarResolucion(String id) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);
        Archivo res = archivos.stream()
                .filter(a -> "RESOLUCION_JPE".equals(a.getTipo()))
                .findFirst().orElseThrow(() -> new Exception("Sin resolución adjunta"));

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("res_jpe_" + id, ".tmp");
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try (InputStream ftpStream = ftpPort.descargarArchivo(res.getRuta() + "/" + res.getNombre());
             java.io.OutputStream tempStream = java.nio.file.Files.newOutputStream(tempFile)) {
            ftpStream.transferTo(tempStream);
        } catch (Exception e) {
            java.nio.file.Files.deleteIfExists(tempFile);
            throw e;
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        InputStream autoDeleteStream = new java.io.FileInputStream(tempFile.toFile()) {
            @Override
            public void close() throws java.io.IOException {
                super.close();
                java.nio.file.Files.deleteIfExists(tempFile);
            }
        };

        return RecursoArchivo.builder()
                .stream(autoDeleteStream)
                .nombreFileName(res.getNombre())
                .build();
    }

    @Override
    public boolean existeDniEnColegio(String dni, String colegioId) {
        return persistencePort.existeDniEnColegio(dni, colegioId);
    }

    private String generarIdCorto() {
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return "J" + timestamp + random;
    }

    private void uploadFile(MultipartFile file, JuezPazEscolar juez, String tipo, String sessionKey) throws Exception {
        String carpeta = switch (tipo.toUpperCase()) {
            case "RESOLUCION_JPE" -> "resoluciones";
            case "FOTO_JPE" -> "fotos";
            default -> "otros";
        };

        String anio = String.valueOf(juez.getFechaRegistro() != null ? juez.getFechaRegistro().getYear() : LocalDate.now().getYear());
        String rutaRelativa = String.format("%s/jpe/alumnos/%s/%s", ftpRutaBase, carpeta, anio);
        String ext = obtenerExtension(file.getOriginalFilename());

        String nombreFinal = juez.getId() + "_" + tipo + "_" + System.currentTimeMillis() + ext;

        if (!ftpPort.uploadFileFTP(sessionKey, rutaRelativa + "/" + nombreFinal, file.getInputStream(), tipo)) {
            throw new Exception("Error al subir archivo al FTP");
        }

        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo)
                .ruta(rutaRelativa)
                .numeroIdentificacion(juez.getId())
                .build());
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }
}