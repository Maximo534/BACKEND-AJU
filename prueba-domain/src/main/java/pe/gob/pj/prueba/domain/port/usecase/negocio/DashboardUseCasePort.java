package pe.gob.pj.prueba.domain.port.usecase.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Dashboard;

public interface DashboardUseCasePort {
    Dashboard obtenerDashboard(int anio, String usuario) throws Exception;
}