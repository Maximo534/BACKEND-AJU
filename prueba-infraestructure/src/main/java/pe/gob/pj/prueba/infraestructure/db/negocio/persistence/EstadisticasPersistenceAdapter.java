package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.port.persistence.negocio.EstadisticasPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.EstadisticasRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EstadisticasPersistenceAdapter implements EstadisticasPersistencePort {

    EstadisticasRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> obtenerDataRanking(String cuo, int anio) {
        List<Object[]> res = repository.obtenerRankingTop10(anio);
        return (res != null) ? res : new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> obtenerDataPorEje(String cuo, int anio) {
        List<Object[]> res = repository.obtenerRankingPorEje(anio);
        return (res != null) ? res : new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> obtenerResumenMagistrado(String cuo, int anio) {
        List<Object[]> res = repository.obtenerResumenActividadMagistrado(anio);
        return (res != null) ? res : new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> obtenerDataDistritos(String cuo, int anio) {
        List<Object[]> res = repository.obtenerRankingDistritos(anio);
        return (res != null) ? res : new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> obtenerEvolucionMensual(String cuo, int anio) {
        List<Object[]> res = repository.obtenerEvolucionMensual(anio);
        return (res != null) ? res : new ArrayList<>();
    }
}