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
    JpeCasoAtendido actualizarCaso(JpeCasoAtendido dominio) throws Exception;
    Pagina<JpeCasoAtendido> listarCasos(String usuario, JpeCasoAtendido filtros, int pagina, int tamanio)throws Exception;
    JpeCasoAtendido buscarCasoPorId(String id) throws Exception;
    JpeCasoAtendido guardarCaso(JpeCasoAtendido caso) throws Exception;
    String obtenerUltimoIdCaso() throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}