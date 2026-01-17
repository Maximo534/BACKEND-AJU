package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;

import java.util.List;

public interface GestionJuecesEscolaresUseCasePort {

    // ✅ CORREGIDO: Recibe JuezPazEscolar (Filtros)
    Pagina<JuezPazEscolar> listar(JuezPazEscolar filtros, int pagina, int tamanio) throws Exception;

    JuezPazEscolar registrar(JuezPazEscolar juez, MultipartFile resolucion, String usuario) throws Exception;
    JuezPazEscolar actualizar(JuezPazEscolar juez, String usuario) throws Exception;
    JuezPazEscolar buscarPorId(String id) throws Exception;

    // Gestión Archivos
    void agregarArchivo(String idJuez, MultipartFile archivo, String tipo, String usuario) throws Exception;
    void eliminarArchivo(String nombreArchivo) throws Exception;
    RecursoArchivo descargarResolucion(String id) throws Exception;

    boolean existeDniEnColegio(String dni, String colegioId);
}