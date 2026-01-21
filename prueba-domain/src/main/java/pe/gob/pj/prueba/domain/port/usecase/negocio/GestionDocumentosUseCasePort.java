package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Documento;

import java.util.List;

public interface GestionDocumentosUseCasePort {

//    Pagina<Documento> listarDocumentos(Documento filtros, int pagina, int tamanio) throws Exception;
    List<Documento> listarDocumentosPorTipo(String tipo) throws Exception;
    Documento registrarDocumento(MultipartFile archivo, Documento documento) throws Exception;
    Documento obtenerDocumento(String id) throws Exception;
    Documento actualizarDocumento(String id, MultipartFile nuevoArchivo, Documento datosNuevos) throws Exception;

    void eliminarDocumento(String id) throws Exception;

    RecursoArchivo descargarDocumento(String id) throws Exception;
}