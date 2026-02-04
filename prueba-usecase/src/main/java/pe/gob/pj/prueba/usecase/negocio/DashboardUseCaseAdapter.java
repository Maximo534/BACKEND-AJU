package pe.gob.pj.prueba.usecase.negocio;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.Dashboard;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DashboardPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.DashboardUseCasePort;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardUseCaseAdapter implements DashboardUseCasePort {

    DashboardPersistencePort persistencePort;

    static final String TX_MANAGER = "txManagerNegocio";
    static final List<String> LABELS_MESES = List.of(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public Dashboard obtenerDashboard(String cuo, int anio, String usuario) {

        List<Integer> statsJI = persistencePort.obtenerEstadisticasJusticiaItinerante(cuo, anio, usuario);
        List<Integer> statsFFC = persistencePort.obtenerEstadisticasFortalecimiento(cuo, anio, usuario);
        List<Integer> statsCultura = persistencePort.obtenerEstadisticasPromocionCultura(cuo, anio, usuario);

        var graficoJI = Dashboard.DetalleGrafico.builder().labels(LABELS_MESES).cantidad(statsJI).build();
        var graficoFFC = Dashboard.DetalleGrafico.builder().labels(LABELS_MESES).cantidad(statsFFC).build();
        var graficoCultura = Dashboard.DetalleGrafico.builder().labels(LABELS_MESES).cantidad(statsCultura).build();

        return Dashboard.builder()
                .anioConsultado(anio)
                .usuarioConsultado(usuario)
                .anualJusticiaItinerante(graficoJI)
                .anualFortalecimiento(graficoFFC)
                .anualPromocion(graficoCultura)
                .build();
    }
}