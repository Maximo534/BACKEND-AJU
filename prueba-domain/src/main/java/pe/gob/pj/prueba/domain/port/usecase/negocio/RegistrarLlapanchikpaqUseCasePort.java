package pe.gob.pj.prueba.domain.port.usecase.negocio;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import java.util.List;

public interface RegistrarLlapanchikpaqUseCasePort {
    LlapanchikpaqJusticia registrar(LlapanchikpaqJusticia dominio, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception;
}