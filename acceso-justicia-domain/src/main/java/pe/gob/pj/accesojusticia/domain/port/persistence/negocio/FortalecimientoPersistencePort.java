package pe.gob.pj.accesojusticia.domain.port.persistence.negocio;

import pe.gob.pj.accesojusticia.domain.model.common.Pagina;
import pe.gob.pj.accesojusticia.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarFortalecimientoQuery;

import java.util.List;

public interface FortalecimientoPersistencePort {
    Pagina<FortalecimientoCapacidades> listar(String cuo, ListarFortalecimientoQuery query, int pagina, int tamanio);

    FortalecimientoCapacidades guardar(String cuo, FortalecimientoCapacidades dominio);
    FortalecimientoCapacidades actualizar(String cuo, FortalecimientoCapacidades dominio);
    FortalecimientoCapacidades obtenerPorId(String cuo, Long id);
    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio);

    List<FortalecimientoCapacidades> listarParaExcel(String cuo, ListarFortalecimientoQuery query);
}