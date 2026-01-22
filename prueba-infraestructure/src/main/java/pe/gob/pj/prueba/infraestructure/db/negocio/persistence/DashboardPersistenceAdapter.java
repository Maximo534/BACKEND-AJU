package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.gob.pj.prueba.domain.model.negocio.Dashboard;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DashboardPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovEventoFcRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJusticiaItineranteRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardPersistenceAdapter implements DashboardPersistencePort {

    private final MovJusticiaItineranteRepository repoJI;
    private final MovEventoFcRepository repoFFC;
    private final MovPromocionCulturaRepository repoCultura;

    @Override
    public List<Dashboard.DataMes> obtenerEstadisticasJusticiaItinerante(int anio, String usuario) throws Exception {
        return procesarMeses(repoJI.contarPorMes(anio, usuario));
    }

    @Override
    public List<Dashboard.DataMes> obtenerEstadisticasFortalecimiento(int anio, String usuario) throws Exception {
        return procesarMeses(repoFFC.contarPorMes(anio, usuario));
    }

    @Override
    public List<Dashboard.DataMes> obtenerEstadisticasPromocionCultura(int anio, String usuario) throws Exception {
        return procesarMeses(repoCultura.contarPorMes(anio, usuario));
    }

    private List<Dashboard.DataMes> procesarMeses(List<Object[]> dataCruda) {
        // Convertir la data de BD a un Mapa <Mes, Cantidad>
        Map<Integer, Integer> mapaDatos = dataCruda.stream()
                .collect(Collectors.toMap(
                        obj -> ((Number) obj[0]).intValue(), // Key: Mes
                        obj -> ((Number) obj[1]).intValue()  // Value: Cantidad
                ));

        // Generar lista del 1 al 12, llenando con 0 si no hay dato en BD
        List<Dashboard.DataMes> resultado = new ArrayList<>();

        for (int mes = 1; mes <= 12; mes++) {
            resultado.add(Dashboard.DataMes.builder()
                    .mes(mes)
                    .cantidad(mapaDatos.getOrDefault(mes, 0))
                    .build());
        }

        return resultado;
    }
}