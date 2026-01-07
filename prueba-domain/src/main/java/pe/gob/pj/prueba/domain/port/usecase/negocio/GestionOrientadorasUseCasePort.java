package pe.gob.pj.prueba.domain.port.usecase.negocio;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;

public interface GestionOrientadorasUseCasePort {
    OrientadoraJudicial registrarAtencion(OrientadoraJudicial dominio, MultipartFile anexo, MultipartFile foto, String usuario) throws Exception;
}