package pe.gob.pj.accesojusticia.domain.port.usecase.negocio;

import pe.gob.pj.accesojusticia.domain.model.negocio.Dashboard;

public interface DashboardUseCasePort {
    Dashboard obtenerDashboard(String cuo, int anio, String usuario) throws Exception;
}