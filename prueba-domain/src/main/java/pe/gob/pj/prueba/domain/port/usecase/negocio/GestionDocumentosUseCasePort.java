package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import java.util.List;

public interface GestionDocumentosUseCasePort {
    List<Documento> listarDocumentos(String tipo);
    Documento registrarDocumento(MultipartFile archivo, String tipo, Integer categoriaId) throws Exception;
}