package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJusticiaItineranteQuery;

public interface JusticiaItinerantePersistencePort {

    Pagina<JusticiaItinerante> listar(String cuo, ListarJusticiaItineranteQuery query, int pagina, int tamanio);
    JusticiaItinerante guardar(String cuo, JusticiaItinerante dominio);
    JusticiaItinerante actualizar(String cuo, JusticiaItinerante dominio);
    JusticiaItinerante obtenerPorId(String cuo, Long id);
    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) ;
}