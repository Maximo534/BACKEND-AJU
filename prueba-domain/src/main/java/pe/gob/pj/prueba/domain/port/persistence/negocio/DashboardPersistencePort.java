package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Dashboard;
import java.util.List;

public interface DashboardPersistencePort {
    List<Dashboard.DataMes> obtenerEstadisticasJusticiaItinerante(int anio, String usuario) throws Exception;
    List<Dashboard.DataMes> obtenerEstadisticasFortalecimiento(int anio, String usuario) throws Exception;
    List<Dashboard.DataMes> obtenerEstadisticasPromocionCultura(int anio, String usuario) throws Exception;
}