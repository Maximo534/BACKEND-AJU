package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface GestionJusticiaPazUseCasePort {

    // ✅ 1. Listado Paginado (Nuevo)
    Pagina<JpeCasoAtendido> listar(String usuario, JpeCasoAtendido filtros, int pagina, int tamanio) throws Exception;

    // ✅ 2. Registrar con Acta y Lista de Fotos (Actualizado)
    JpeCasoAtendido registrar(JpeCasoAtendido caso, MultipartFile acta, List<MultipartFile> fotos, String usuario) throws Exception;

    // ✅ 3. Actualizar Datos (Nuevo)
    JpeCasoAtendido actualizar(JpeCasoAtendido caso, String usuario) throws Exception;

    // Para eliminar archivos al editar (Nuevo)
    void eliminarArchivo(String nombreArchivo) throws Exception;

    // Para agregar archivos extra al editar (Nuevo)
    void agregarArchivo(String idCaso, MultipartFile archivo, String tipo, String usuario) throws Exception;

    // Para descargar Acta o Resolución
    RecursoArchivo descargarArchivoPorTipo(String id, String tipoArchivo) throws Exception;

    JpeCasoAtendido buscarPorId(String id) throws Exception;

    byte[] generarFichaPdf(String id) throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}
