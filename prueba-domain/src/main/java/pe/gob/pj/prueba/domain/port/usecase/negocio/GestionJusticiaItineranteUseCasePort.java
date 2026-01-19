package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import java.util.List;

public interface GestionJusticiaItineranteUseCasePort {

    Pagina<JusticiaItinerante> listar(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception;
    JusticiaItinerante registrar(JusticiaItinerante dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception;
    JusticiaItinerante buscarPorId(String id) throws Exception;
    JusticiaItinerante actualizar(JusticiaItinerante fji, String usuarioOperacion) throws Exception;
    void agregarArchivo(String idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;
    void eliminarArchivo(String nombreArchivo) throws Exception;
    RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception;
    byte[] generarFichaPdf(String idEvento) throws Exception;
}