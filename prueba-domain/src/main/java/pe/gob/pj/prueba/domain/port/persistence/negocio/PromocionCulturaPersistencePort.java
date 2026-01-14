package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;

public interface PromocionCulturaPersistencePort {

    /**
     * Guarda la cabecera y todos sus detalles.
     * @param promocionCultura Objeto de dominio.
     * @return El objeto guardado.
     */
    PromocionCultura guardar(PromocionCultura promocionCultura) throws Exception;

    PromocionCultura actualizar(PromocionCultura promocionCultura) throws Exception;

    PromocionCultura obtenerPorId(String id) throws Exception;

    /**
     * Lista paginada usando el objeto de dominio como contenedor de filtros.
     * @param usuario Usuario auditoría.
     * @param filtros Objeto con los criterios de búsqueda (id, descripción, fechas, etc.).
     * @param pagina Número de página actual.
     * @param tamanio Cantidad de registros por página.
     * @return Página de resultados.
     */
    Pagina<PromocionCultura> listar(String usuario, PromocionCultura filtros, int pagina, int tamanio) throws Exception;

    String obtenerUltimoId() throws Exception;
}