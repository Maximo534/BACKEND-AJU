package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;

public interface JusticiaItinerantePersistencePort {

    /**
     * Guarda la cabecera y todos sus detalles.
     * @param justiciaItinerante Objeto de dominio.
     * @return El objeto guardado.
     */
    JusticiaItinerante guardar(JusticiaItinerante justiciaItinerante) throws Exception;

    JusticiaItinerante actualizar(JusticiaItinerante justiciaItinerante) throws Exception;

    JusticiaItinerante obtenerPorId(String id) throws Exception;
    Pagina<JusticiaItinerante> listar(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception;
    String obtenerUltimoId() throws Exception;
}