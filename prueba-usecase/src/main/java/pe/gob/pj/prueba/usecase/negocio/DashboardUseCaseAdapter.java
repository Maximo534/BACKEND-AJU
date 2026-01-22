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

    private static final List<String> LABELS_MESES = List.of(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    @Override
    @Transactional(readOnly = true)
    public Dashboard obtenerDashboard(int anio, String usuario) throws Exception {

        List<Integer> statsJI = persistencePort.obtenerEstadisticasJusticiaItinerante(anio, usuario);
        List<Integer> statsFFC = persistencePort.obtenerEstadisticasFortalecimiento(anio, usuario);
        List<Integer> statsCultura = persistencePort.obtenerEstadisticasPromocionCultura(anio, usuario);

        var graficoJI = Dashboard.DetalleGrafico.builder()
                .labels(LABELS_MESES)
                .cantidad(statsJI)
                .build();

        var graficoFFC = Dashboard.DetalleGrafico.builder()
                .labels(LABELS_MESES)
                .cantidad(statsFFC)
                .build();

        var graficoCultura = Dashboard.DetalleGrafico.builder()
                .labels(LABELS_MESES)
                .cantidad(statsCultura)
                .build();

        return Dashboard.builder()
                .anioConsultado(anio)
                .usuarioConsultado(usuario)
                .anualJusticiaItinerante(graficoJI)
                .anualFortalecimiento(graficoFFC)
                .anualPromocion(graficoCultura)
                .build();
    }
}