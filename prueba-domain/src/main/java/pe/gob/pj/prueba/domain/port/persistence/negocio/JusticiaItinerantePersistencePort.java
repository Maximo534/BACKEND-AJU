package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJusticiaItineranteQuery;

import java.util.List;

public interface JusticiaItinerantePersistencePort {

    Pagina<JusticiaItinerante> listar(String cuo, ListarJusticiaItineranteQuery query, int pagina, int tamanio);
    List<JusticiaItinerante> listarParaExcel(String cuo, ListarJusticiaItineranteQuery query);
    JusticiaItinerante guardar(String cuo, JusticiaItinerante dominio);
    JusticiaItinerante actualizar(String cuo, JusticiaItinerante dominio);
    JusticiaItinerante obtenerPorId(String cuo, Long id);
    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) ;
}