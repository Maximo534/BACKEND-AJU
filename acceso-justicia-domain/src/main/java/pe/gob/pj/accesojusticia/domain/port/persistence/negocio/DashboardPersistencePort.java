package pe.gob.pj.accesojusticia.domain.port.persistence.negocio;

import java.util.List;

public interface DashboardPersistencePort {
    List<Integer> obtenerEstadisticasJusticiaItinerante(String cuo, int anio, String usuario);
    List<Integer> obtenerEstadisticasFortalecimiento(String cuo, int anio, String usuario);
    List<Integer> obtenerEstadisticasPromocionCultura(String cuo, int anio, String usuario);
}