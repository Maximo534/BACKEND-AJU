package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJpeCasosQuery;

import java.util.List;

public interface GestionJusticiaPazUseCasePort {

    Pagina<JpeCasoAtendido> listar(String cuo, ListarJpeCasosQuery query, int pagina, int tamanio);

    JpeCasoAtendido buscarPorId(String cuo, Long id);

    JpeCasoAtendido registrar(String cuo, JpeCasoAtendido dominio, MultipartFile acta, List<MultipartFile> fotos) throws Exception;

    JpeCasoAtendido actualizar(String cuo, JpeCasoAtendido dominio) throws Exception;

    void agregarArchivo(String cuo, Long idCaso, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;

    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;

    RecursoArchivo descargarArchivoPorTipo(String cuo, Long idCaso, String tipoArchivo) throws Exception;

    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception;

    byte[] generarFichaPdf(String cuo, Long idCaso) throws Exception;

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}