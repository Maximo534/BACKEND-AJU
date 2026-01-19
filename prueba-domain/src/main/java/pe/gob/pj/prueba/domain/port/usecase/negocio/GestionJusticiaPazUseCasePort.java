package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface GestionJusticiaPazUseCasePort {
    Pagina<JpeCasoAtendido> listar(String usuario, JpeCasoAtendido filtros, int pagina, int tamanio) throws Exception;
    JpeCasoAtendido registrar(JpeCasoAtendido caso, MultipartFile acta, List<MultipartFile> fotos, String usuario) throws Exception;
    JpeCasoAtendido actualizar(JpeCasoAtendido caso, String usuario) throws Exception;
    void eliminarArchivo(String nombreArchivo) throws Exception;
    void agregarArchivo(String idCaso, MultipartFile archivo, String tipo, String usuario) throws Exception;
    RecursoArchivo descargarArchivoPorTipo(String id, String tipoArchivo) throws Exception;
    JpeCasoAtendido buscarPorId(String id) throws Exception;
    byte[] generarFichaPdf(String id) throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}
