package pe.gob.pj.accesojusticia.domain.port.persistence.negocio;

import java.util.List;

public interface EstadisticasPersistencePort {
    List<Object[]> obtenerDataRanking(String cuo, int anio);
    List<Object[]> obtenerDataPorEje(String cuo, int anio);
    List<Object[]> obtenerResumenMagistrado(String cuo, int anio);
    List<Object[]> obtenerDataDistritos(String cuo, int anio);
    List<Object[]> obtenerEvolucionMensual(String cuo, int anio);
}