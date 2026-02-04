package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarBuenaPracticaQuery;

import java.util.List;

public interface GestionBuenaPracticaUseCasePort {

    Pagina<BuenaPractica> listar(String cuo, ListarBuenaPracticaQuery query, int pagina, int tamanio);

    BuenaPractica registrar(String cuo, BuenaPractica dominio, MultipartFile anexo, MultipartFile ppt, List<MultipartFile> fotos, MultipartFile video) throws Exception;

    BuenaPractica buscarPorId(String cuo, Long id);

    BuenaPractica actualizar(String cuo, BuenaPractica dominio) throws Exception;

    void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;

    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;

    RecursoArchivo descargarArchivoPorTipo(String cuo, Long idEvento, String tipoArchivo) throws Exception;

    byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception;

    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception;

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}
