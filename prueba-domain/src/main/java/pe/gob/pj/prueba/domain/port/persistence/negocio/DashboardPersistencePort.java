package pe.gob.pj.prueba.domain.port.persistence.negocio;

import java.util.List;

public interface DashboardPersistencePort {
    // Debe retornar List<Integer>, NO List<DataMes>
    List<Integer> obtenerEstadisticasJusticiaItinerante(int anio, String usuario) throws Exception;
    List<Integer> obtenerEstadisticasFortalecimiento(int anio, String usuario) throws Exception;
    List<Integer> obtenerEstadisticasPromocionCultura(int anio, String usuario) throws Exception;
}