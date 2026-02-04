package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarOrientadoraQuery;

import java.util.List;

public interface GestionOrientadorasUseCasePort {

    Pagina<OrientadoraJudicial> listar(String cuo, ListarOrientadoraQuery query, int pagina, int tamanio);

    OrientadoraJudicial buscarPorId(String cuo, Long id);

    OrientadoraJudicial registrarAtencion(String cuo, OrientadoraJudicial oj, MultipartFile anexo, List<MultipartFile> fotos) throws Exception;

    OrientadoraJudicial actualizar(String cuo, OrientadoraJudicial dominio) throws Exception;

    void agregarArchivo(String cuo, Long idCaso, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;

    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;

    RecursoArchivo descargarArchivoPorTipo(String cuo, Long idCaso, String tipoArchivo) throws Exception;

    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception;

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;

    byte[] generarFichaPdf(String cuo, Long id) throws Exception;
}