package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DashboardPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovEventoFcRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJusticiaItineranteRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardPersistenceAdapter implements DashboardPersistencePort {

    private final MovJusticiaItineranteRepository repoJI;
    private final MovEventoFcRepository repoFFC;
    private final MovPromocionCulturaRepository repoCultura;

    @Override
    public List<Integer> obtenerEstadisticasJusticiaItinerante(int anio, String usuario) throws Exception {
        return procesarMeses(repoJI.contarPorMes(anio, usuario));
    }

    @Override
    public List<Integer> obtenerEstadisticasFortalecimiento(int anio, String usuario) throws Exception {
        return procesarMeses(repoFFC.contarPorMes(anio, usuario));
    }

    @Override
    public List<Integer> obtenerEstadisticasPromocionCultura(int anio, String usuario) throws Exception {
        return procesarMeses(repoCultura.contarPorMes(anio, usuario));
    }

    private List<Integer> procesarMeses(List<Object[]> dataCruda) {
        Integer[] meses = new Integer[12];
        Arrays.fill(meses, 0);

        if (dataCruda != null) {
            for (Object[] fila : dataCruda) {
                if (fila[0] != null) {
                    // Restamos 1 porque la BD devuelve Enero=1 pero el array empieza en 0
                    int mesIndex = ((Number) fila[0]).intValue() - 1;
                    int cantidad = ((Number) fila[1]).intValue();

                    if (mesIndex >= 0 && mesIndex < 12) {
                        meses[mesIndex] = cantidad;
                    }
                }
            }
        }
        return Arrays.asList(meses);
    }
}