package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.Dashboard;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DashboardPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.DashboardUseCasePort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardUseCaseAdapter implements DashboardUseCasePort {

    private final DashboardPersistencePort persistencePort;

    @Override
    @Transactional(readOnly = true)
    public Dashboard obtenerDashboard(int anio, String usuario) throws Exception {

        List<Integer> statsJI = persistencePort.obtenerEstadisticasJusticiaItinerante(anio, usuario);
        List<Integer> statsFFC = persistencePort.obtenerEstadisticasFortalecimiento(anio, usuario);
        List<Integer> statsCultura = persistencePort.obtenerEstadisticasPromocionCultura(anio, usuario);

        var widgetGrafico = Dashboard.GraficoBarras.builder()
                .justiciaItinerante(statsJI)
                .fortalecimiento(statsFFC)
                .promocionCultura(statsCultura)
                .build();

        return Dashboard.builder()
                .anioConsultado(anio)
                .usuarioConsultado(usuario)
                .graficoAnual(widgetGrafico)
                .build();
    }
}