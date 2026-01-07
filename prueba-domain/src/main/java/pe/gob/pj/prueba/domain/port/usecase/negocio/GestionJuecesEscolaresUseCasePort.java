package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface GestionJuecesEscolaresUseCasePort {

    // ==========================================
    // SECCIÓN 1: GESTIÓN DE JUECES (ALUMNOS)
    // ==========================================
    JuezPazEscolar registrarJuez(JuezPazEscolar juez, MultipartFile foto, MultipartFile resolucion, String usuarioOperacion) throws Exception;

    List<JuezPazEscolar> listarJuecesPorColegio(String colegioId);


    // ==========================================
    // SECCIÓN 2: GESTIÓN DE CASOS (INCIDENTES)
    // ==========================================

    // ✅ 1. Listado Paginado (Nuevo)
    Pagina<JpeCasoAtendido> listarCasos(JpeCasoAtendido filtros, int pagina, int tamanio) throws Exception;

    // ✅ 2. Registrar con Acta y Lista de Fotos (Actualizado)
    JpeCasoAtendido registrarCaso(JpeCasoAtendido caso, MultipartFile acta, List<MultipartFile> fotos, String usuario) throws Exception;

    // ✅ 3. Actualizar Datos (Nuevo)
    JpeCasoAtendido actualizarCaso(JpeCasoAtendido caso, String usuario) throws Exception;


    // ==========================================
    // SECCIÓN 3: GESTIÓN DE ARCHIVOS (Común)
    // ==========================================

    // Para eliminar archivos al editar (Nuevo)
    void eliminarArchivo(String nombreArchivo) throws Exception;

    // Para agregar archivos extra al editar (Nuevo)
    void subirArchivoAdicional(String idCaso, MultipartFile archivo, String tipoArchivo, String usuario) throws Exception;

    // Para descargar Acta o Resolución
    RecursoArchivo descargarArchivoPorTipo(String id, String tipoArchivo) throws Exception;

    JpeCasoAtendido buscarCasoPorId(String id) throws Exception;

    byte[] generarFichaPdf(String id) throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}