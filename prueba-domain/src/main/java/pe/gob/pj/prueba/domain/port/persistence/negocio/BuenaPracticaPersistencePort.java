package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarBuenaPracticaQuery;

import java.util.List;

public interface BuenaPracticaPersistencePort {

    Pagina<BuenaPractica> listar(String cuo, ListarBuenaPracticaQuery query, int pagina, int tamanio);

    BuenaPractica guardar(String cuo, BuenaPractica dominio);

    BuenaPractica actualizar(String cuo, BuenaPractica dominio);

    BuenaPractica obtenerPorId(String cuo, Long id);

    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio);

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}
