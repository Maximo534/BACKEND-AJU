package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJusticiaItineranteQuery;

import java.util.List;

public interface GestionJusticiaItineranteUseCasePort {

    Pagina<JusticiaItinerante> listar(String cuo, ListarJusticiaItineranteQuery query, int pagina, int tamanio);
    JusticiaItinerante registrar(String cuo, JusticiaItinerante dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception;
    JusticiaItinerante buscarPorId(String cuo, Long id);
    JusticiaItinerante actualizar(String cuo, JusticiaItinerante dominio) throws Exception;
    void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;
    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;
    RecursoArchivo descargarAnexo(String cuo, Long idEvento) throws Exception;
    byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception;
    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception ;

    byte[] exportarExcel(String cuo, ListarJusticiaItineranteQuery query) throws Exception;
}