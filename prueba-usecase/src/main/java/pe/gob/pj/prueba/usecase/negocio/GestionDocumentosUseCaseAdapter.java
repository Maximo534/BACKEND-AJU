package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.exceptions.negocio.MovimientoNoEncontradoException;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DocumentoPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionDocumentosUseCasePort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
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

    private static final String RUTA_BASE_DOCUMENTOS = "/evidencias/documentos_gestion";
    private static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Documento> listarDocumentosPorTipo(String cuo, String tipo) throws Exception {
        return persistencePort.listarPorTipo(cuo, tipo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Documento obtenerDocumento(String cuo, Long id) throws Exception {
        Documento doc = persistencePort.buscarPorId(cuo, id);
        if (doc == null) {
            throw new MovimientoNoEncontradoException("No se encontró el documento con ID: " + id);
        }
        return doc;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public Documento registrarDocumento(String cuo, MultipartFile archivo, Documento documento, String usuario) throws Exception {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("El archivo es obligatorio.");

        int anioActual = LocalDate.now().getYear();
        documento.setPeriodo(anioActual);
        documento.setUsuario(usuario); // Auditoría

        String originalFilename = archivo.getOriginalFilename();
        String extension = obtenerExtension(originalFilename);

        String nuevoNombre = UUID.randomUUID() + extension;
        String rutaCompletaArchivo = String.format("%s/%d/%s", RUTA_BASE_DOCUMENTOS, anioActual, nuevoNombre);

        // 1. Subir al FTP usando el adaptador del Proyecto Base
        subirAlFtp(cuo, rutaCompletaArchivo, archivo.getInputStream());

        // 2. Metadata de Negocio
        documento.setNombre(originalFilename);
        documento.setFormato(extension.replace(".", "").toUpperCase());
        documento.setRuta(rutaCompletaArchivo);
        documento.setActivo("1");

        return persistencePort.guardar(cuo, documento);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public Documento actualizarDocumento(String cuo, Long id, MultipartFile nuevoArchivo, Documento datosNuevos, String usuario) throws Exception {
        Documento docExistente = obtenerDocumento(cuo, id);
        docExistente.setUsuario(usuario);

        if (nuevoArchivo != null && !nuevoArchivo.isEmpty()) {
            String ext = obtenerExtension(nuevoArchivo.getOriginalFilename());
            String nuevoNombreFisico = UUID.randomUUID() + ext;
            String nuevaRuta = String.format("%s/%d/%s", RUTA_BASE_DOCUMENTOS, docExistente.getPeriodo(), nuevoNombreFisico);

            // Subimos la nueva versión (sin borrar el histórico físico)
            subirAlFtp(cuo, nuevaRuta, nuevoArchivo.getInputStream());

            docExistente.setRuta(nuevaRuta);
            docExistente.setNombre(nuevoArchivo.getOriginalFilename());
            docExistente.setFormato(ext.replace(".", "").toUpperCase());
        }

        docExistente.setTipo(datosNuevos.getTipo());
        docExistente.setCategoriaDocumentoId(datosNuevos.getCategoriaDocumentoId());

        return persistencePort.guardar(cuo, docExistente);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarDocumento(String cuo, Long id, String usuario) throws Exception {
        Documento doc = obtenerDocumento(cuo, id);
        doc.setUsuario(usuario);
        doc.setActivo("0");
        persistencePort.guardar(cuo, doc);
    }

    @Override
    public RecursoArchivo descargarDocumento(String cuo, Long id) throws Exception {
        Documento doc = obtenerDocumento(cuo, id);

        // Descargar usando el adaptador del Proyecto Base
        InputStream stream = descargarDelFtp(cuo, doc.getRuta());

        return RecursoArchivo.builder()
                .stream(stream)
                .nombreFileName(doc.getNombre())
                .build();
    }


    private void subirAlFtp(String cuo, String ruta, InputStream stream) throws Exception {
        // En tu código original usabas un UUID como sessionKey
        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            if (!ftpPort.uploadFileFTP(sessionKey, ruta, stream, "Carga Doc Gestion")) {
                throw new Exception("Error al subir archivo al FTP base.");
            }
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    private InputStream descargarDelFtp(String cuo, String ruta) throws Exception {
        String sessionKey = UUID.randomUUID().toString();
        byte[] fileBytes;
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            fileBytes = ftpPort.downloadFileBytes(sessionKey, ruta);

            if (fileBytes == null) throw new Exception("El archivo no existe en el FTP base.");

        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
        return new ByteArrayInputStream(fileBytes);
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : ".dat";
    }
}