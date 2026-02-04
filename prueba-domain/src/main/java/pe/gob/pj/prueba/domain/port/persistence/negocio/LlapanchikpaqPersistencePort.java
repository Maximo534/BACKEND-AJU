package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarLlapanchikpaqQuery;

import java.util.List;

public interface LlapanchikpaqPersistencePort {

    Pagina<LlapanchikpaqJusticia> listar(String cuo, ListarLlapanchikpaqQuery query, int pagina, int tamanio);

    LlapanchikpaqJusticia guardar(String cuo, LlapanchikpaqJusticia dominio);

    LlapanchikpaqJusticia actualizar(String cuo, LlapanchikpaqJusticia dominio);

    LlapanchikpaqJusticia obtenerPorId(String cuo, Long id);

    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio);

     List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;;
}
