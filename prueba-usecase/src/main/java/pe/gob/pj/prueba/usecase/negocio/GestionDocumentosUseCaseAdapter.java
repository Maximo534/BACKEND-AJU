package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DocumentoPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionDocumentosUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionDocumentosUseCaseAdapter implements GestionDocumentosUseCasePort {

    private final DocumentoPersistencePort persistencePort;
    private final FtpPort ftpPort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    @Transactional(readOnly = true)
    public Pagina<Documento> listarDocumentos(Documento filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listarConFiltros(filtros, pagina, tamanio);
    }

    @Override
    @Transactional
    public Documento registrarDocumento(MultipartFile archivo, Documento documento) throws Exception {
        int anioActual = LocalDate.now().getYear();
        documento.setPeriodo(anioActual);

        String originalFilename = archivo.getOriginalFilename();
        String extension = obtenerExtension(originalFilename);

        // Ruta FTP: /evidencias/documentos_gestion/2026/UUID.pdf
        String rutaCarpetaFtp = String.format("%s/documentos_gestion/%d", ftpRutaBase, anioActual);
        String nombreUnico = UUID.randomUUID().toString() + extension;
        String rutaCompletaArchivo = rutaCarpetaFtp + "/" + nombreUnico;

        subirAlFtp(rutaCompletaArchivo, archivo.getInputStream());

        documento.setNombre(originalFilename);
        documento.setFormato(extension.replace(".", "").toUpperCase());
        documento.setRutaArchivo(rutaCompletaArchivo);
        documento.setActivo("1");

        return persistencePort.guardar(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public Documento obtenerDocumento(String id) throws Exception {
        Documento doc = persistencePort.buscarPorId(id);
        if (doc == null) throw new Exception("Documento no encontrado con ID: " + id);
        return doc;
    }

    @Override
    @Transactional
    public Documento actualizarDocumento(String id, MultipartFile nuevoArchivo, Documento datosNuevos) throws Exception {
        Documento docExistente = persistencePort.buscarPorId(id);
        if (docExistente == null) throw new Exception("Documento no encontrado.");

        if (nuevoArchivo != null && !nuevoArchivo.isEmpty()) {
            // Borrar archivo físico
            try { eliminarDelFtp(docExistente.getRutaArchivo()); } catch (Exception e) { log.warn("No se borró archivo viejo FTP"); }

            // Subir nuevo
            int anio = docExistente.getPeriodo();
            String ext = obtenerExtension(nuevoArchivo.getOriginalFilename());
            String nuevoNombre = UUID.randomUUID().toString() + ext;
            String nuevaRuta = String.format("%s/documentos_gestion/%d/%s", ftpRutaBase, anio, nuevoNombre);

            subirAlFtp(nuevaRuta, nuevoArchivo.getInputStream());

            docExistente.setRutaArchivo(nuevaRuta);
            docExistente.setNombre(nuevoArchivo.getOriginalFilename());
            docExistente.setFormato(ext.replace(".", "").toUpperCase());
        }

        docExistente.setTipo(datosNuevos.getTipo());
        docExistente.setCategoriaId(datosNuevos.getCategoriaId());

        return persistencePort.guardar(docExistente);
    }

    @Override
    @Transactional
    public void eliminarDocumento(String id) throws Exception {
        Documento doc = persistencePort.buscarPorId(id);
        if (doc != null) {
            doc.setActivo("0");
            persistencePort.guardar(doc);
        }
    }

    @Override
    public RecursoArchivo descargarDocumento(String id) throws Exception {
        Documento doc = persistencePort.buscarPorId(id);
        if (doc == null) throw new Exception("Documento no encontrado");

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("doc_" + id, ".tmp");
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try (InputStream ftpStream = ftpPort.descargarArchivo(doc.getRutaArchivo());
             java.io.OutputStream tempFileStream = java.nio.file.Files.newOutputStream(tempFile)) {
            ftpStream.transferTo(tempFileStream);
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
                .nombreFileName(doc.getNombre())
                .build();
    }

    private void subirAlFtp(String ruta, InputStream stream) throws Exception {
        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            if (!ftpPort.uploadFileFTP(sessionKey, ruta, stream, "Carga Doc")) {
                throw new Exception("Error al subir archivo al FTP");
            }
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    private void eliminarDelFtp(String ruta) throws Exception {
        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            ftpPort.deleteFileFTP(ruta);
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : ".dat";
    }
}