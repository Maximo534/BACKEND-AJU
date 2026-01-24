package pe.gob.pj.prueba.domain.port.usecase.negocio;

import pe.gob.pj.prueba.domain.model.negocio.DetalleGrafico;
import pe.gob.pj.prueba.domain.model.negocio.EstadisticasData;

public interface EstadisticasUseCasePort {
    EstadisticasData obtenerEstadisticasCompletas(int anio) throws Exception;
}
