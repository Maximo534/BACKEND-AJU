package pe.gob.pj.accesojusticia.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.accesojusticia.domain.model.common.Pagina;
import pe.gob.pj.accesojusticia.domain.model.common.RecursoArchivo;
import pe.gob.pj.accesojusticia.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.accesojusticia.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarLlapanchikpaqQuery;

import java.util.List;

public interface GestionLlapanchikpaqUseCasePort {

    Pagina<LlapanchikpaqJusticia> listar(String cuo, ListarLlapanchikpaqQuery query, int pagina, int tamanio);

    LlapanchikpaqJusticia registrar(String cuo, LlapanchikpaqJusticia dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception;

    LlapanchikpaqJusticia buscarPorId(String cuo, Long id);

    LlapanchikpaqJusticia actualizar(String cuo, LlapanchikpaqJusticia dominio) throws Exception;

    void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;

    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;

    RecursoArchivo descargarArchivoPorTipo(String cuo, Long idEvento, String tipoArchivo) throws Exception;

    byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception;

    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception;

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;

}
