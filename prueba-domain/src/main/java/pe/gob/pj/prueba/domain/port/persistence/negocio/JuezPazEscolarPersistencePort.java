package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import java.util.List;

public interface JuezPazEscolarPersistencePort {
    Pagina<JuezPazEscolar> listar(JuezPazEscolar filtros, int pagina, int tamanio);
    JuezPazEscolar guardar(JuezPazEscolar domain) throws Exception;
    JuezPazEscolar actualizar(JuezPazEscolar domain) throws Exception;
    JuezPazEscolar buscarPorId(String id) throws Exception;
    boolean existeDniEnColegio(String dni, String colegioId);
}