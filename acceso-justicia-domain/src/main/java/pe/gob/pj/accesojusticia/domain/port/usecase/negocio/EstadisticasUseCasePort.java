package pe.gob.pj.accesojusticia.domain.port.usecase.negocio;

import pe.gob.pj.accesojusticia.domain.model.negocio.EstadisticasData;

public interface EstadisticasUseCasePort {
    EstadisticasData obtenerEstadisticasCompletas(String cuo, int anio);
}
