package pe.gob.pj.prueba.domain.port.persistence.negocio;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface OrientadoraJudicialPersistencePort {
    Pagina<OrientadoraJudicial> listar(String usuario, OrientadoraJudicial filtros, int pagina, int tamanio) throws Exception;
    OrientadoraJudicial guardar(OrientadoraJudicial dominio) throws Exception;
    String obtenerUltimoId() throws Exception;
    OrientadoraJudicial buscarPorId(String id) throws Exception;
    OrientadoraJudicial actualizar(OrientadoraJudicial dominio) throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;

}