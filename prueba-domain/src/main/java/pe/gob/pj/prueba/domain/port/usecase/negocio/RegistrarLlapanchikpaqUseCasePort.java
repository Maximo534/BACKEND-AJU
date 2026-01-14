package pe.gob.pj.prueba.domain.port.usecase.negocio;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface RegistrarLlapanchikpaqUseCasePort {
    Pagina<LlapanchikpaqJusticia> listar(String usuario, LlapanchikpaqJusticia filtros, int pagina, int tamanio) throws Exception;
    LlapanchikpaqJusticia buscarPorId(String id) throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
    byte[] generarFichaPdf(String id) throws Exception;

    LlapanchikpaqJusticia registrar(LlapanchikpaqJusticia dominio, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception;
    LlapanchikpaqJusticia actualizar(LlapanchikpaqJusticia dominio, String usuario) throws Exception;
    void eliminarArchivo(String nombreArchivo) throws Exception;
    void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception;
    RecursoArchivo descargarArchivoPorTipo(String id, String tipo) throws Exception;

}