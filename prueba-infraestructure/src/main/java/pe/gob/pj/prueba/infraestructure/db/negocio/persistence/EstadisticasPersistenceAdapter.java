package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.pj.prueba.domain.port.persistence.negocio.EstadisticasPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.EstadisticasRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EstadisticasPersistenceAdapter implements EstadisticasPersistencePort {

    private final EstadisticasRepository repository;

    @Override
    public List<Object[]> obtenerDataRanking(int anio) throws Exception {
        List<Object[]> resultado = repository.obtenerRankingTop10(anio);
        return (resultado != null) ? resultado : new ArrayList<>();
    }

    @Override
    public List<Object[]> obtenerDataPorEje(int anio) throws Exception {
        List<Object[]> resultado = repository.obtenerRankingPorEje(anio);
        return (resultado != null) ? resultado : new ArrayList<>();
    }

    @Override
    public List<Object[]> obtenerResumenMagistrado(int anio) throws Exception {
        List<Object[]> resultado = repository.obtenerResumenActividadMagistrado(anio);
        return (resultado != null) ? resultado : new ArrayList<>();
    }

    @Override
    public List<Object[]> obtenerDataDistritos(int anio) throws Exception {
        List<Object[]> resultado = repository.obtenerRankingDistritos(anio);
        return (resultado != null) ? resultado : new ArrayList<>();
    }

    @Override
    public List<Object[]> obtenerEvolucionMensual(int anio) throws Exception {
        List<Object[]> resultado = repository.obtenerEvolucionMensual(anio);
        return (resultado != null) ? resultado : new ArrayList<>();
    }
}