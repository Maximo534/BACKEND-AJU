package pe.gob.pj.prueba.domain.port.persistence.negocio;

import java.util.List;

public interface EstadisticasPersistencePort {
    List<Object[]> obtenerDataRanking(int anio) throws Exception;

    List<Object[]> obtenerDataPorEje(int anio) throws Exception;

    List<Object[]> obtenerResumenMagistrado(int anio) throws Exception;

    List<Object[]> obtenerDataDistritos(int anio) throws Exception;
    List<Object[]> obtenerEvolucionMensual(int anio) throws Exception;
}