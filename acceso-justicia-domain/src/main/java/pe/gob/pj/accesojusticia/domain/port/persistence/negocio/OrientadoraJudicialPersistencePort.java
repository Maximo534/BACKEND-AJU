package pe.gob.pj.accesojusticia.domain.port.persistence.negocio;

import pe.gob.pj.accesojusticia.domain.model.common.Pagina;
import pe.gob.pj.accesojusticia.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.accesojusticia.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarOrientadoraQuery;

import java.util.List;

public interface OrientadoraJudicialPersistencePort {

    Pagina<OrientadoraJudicial> listar(String cuo, ListarOrientadoraQuery query, int pagina, int tamanio);

    OrientadoraJudicial guardar(String cuo, OrientadoraJudicial dominio);

    OrientadoraJudicial actualizar(String cuo, OrientadoraJudicial dominio);

    OrientadoraJudicial obtenerPorId(String cuo, Long id);

    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio);

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}