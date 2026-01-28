package pe.gob.pj.prueba.domain.port.usecase.negocio;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface GestionOrientadorasUseCasePort {
    Pagina<OrientadoraJudicial> listar(String usuario, OrientadoraJudicial filtros, int pagina, int tamanio) throws Exception;
    OrientadoraJudicial registrarAtencion(OrientadoraJudicial dominio, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception;
    OrientadoraJudicial buscarPorId(String id) throws Exception;
    OrientadoraJudicial actualizar(OrientadoraJudicial dominio, String usuario) throws Exception;
    void agregarArchivo(String idCaso, MultipartFile archivo, String tipo, String usuario) throws Exception;
    void eliminarArchivo(String nombreArchivo) throws Exception;
    RecursoArchivo descargarAnexo(String id, String tipoArchivo) throws Exception;
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
    byte[] generarFichaPdf(String id) throws Exception;
    RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception;
}