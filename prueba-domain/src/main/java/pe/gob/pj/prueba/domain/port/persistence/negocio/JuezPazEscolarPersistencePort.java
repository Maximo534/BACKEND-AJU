package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface JuezPazEscolarPersistencePort {
    JuezPazEscolar guardar(JuezPazEscolar juez) throws Exception;
    boolean existeDniEnColegio(String dni, String colegioId);
    List<JuezPazEscolar> listarPorColegio(String colegioId);

    String obtenerUltimoIdCaso();
    JpeCasoAtendido guardarCaso(JpeCasoAtendido caso) throws Exception;
    JpeCasoAtendido buscarCasoPorId(String id) throws Exception;

    Pagina<JpeCasoAtendido> listarCasos(JpeCasoAtendido filtros, int pagina, int tamanio);
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}