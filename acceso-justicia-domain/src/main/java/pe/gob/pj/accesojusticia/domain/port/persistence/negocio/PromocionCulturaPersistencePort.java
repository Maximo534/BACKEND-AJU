package pe.gob.pj.accesojusticia.domain.port.persistence.negocio;

import pe.gob.pj.accesojusticia.domain.model.common.Pagina;
import pe.gob.pj.accesojusticia.domain.model.negocio.PromocionCultura;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarPromocionQuery;

import java.util.List;

public interface PromocionCulturaPersistencePort {
    Pagina<PromocionCultura> listar(String cuo, ListarPromocionQuery query, int pagina, int tamanio);

    PromocionCultura guardar(String cuo, PromocionCultura dominio);

    PromocionCultura actualizar(String cuo, PromocionCultura dominio) ;

    PromocionCultura obtenerPorId(String cuo, Long id) ;

    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio);

    List<PromocionCultura> listarParaExcel(String cuo, ListarPromocionQuery query);
}