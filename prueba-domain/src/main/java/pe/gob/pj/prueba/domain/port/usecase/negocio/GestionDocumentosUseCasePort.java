package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Documento;

import java.util.List;

public interface GestionDocumentosUseCasePort {

    List<Documento> listarDocumentosPorTipo(String cuo, String tipo) throws Exception;

    Documento obtenerDocumento(String cuo, Long id) throws Exception;

    Documento registrarDocumento(String cuo, MultipartFile archivo, Documento documento, String usuario) throws Exception;

    Documento actualizarDocumento(String cuo, Long id, MultipartFile nuevoArchivo, Documento datosNuevos, String usuario) throws Exception;

    void eliminarDocumento(String cuo, Long id, String usuario) throws Exception;

    RecursoArchivo descargarDocumento(String cuo, Long id) throws Exception;
}