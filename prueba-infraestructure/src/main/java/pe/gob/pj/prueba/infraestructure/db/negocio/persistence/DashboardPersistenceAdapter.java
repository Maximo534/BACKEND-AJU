package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DashboardPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovEventoFcRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJusticiaItineranteRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardPersistenceAdapter implements DashboardPersistencePort {

    MovJusticiaItineranteRepository repoJI;
    MovEventoFcRepository repoFFC;
    MovPromocionCulturaRepository repoCultura;

    @Override
    @Transactional(readOnly = true)
    public List<Integer> obtenerEstadisticasJusticiaItinerante(String cuo, int anio, String usuario) {
        return procesarMeses(repoJI.contarPorMes(anio, usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> obtenerEstadisticasFortalecimiento(String cuo, int anio, String usuario) {
        return procesarMeses(repoFFC.contarPorMes(anio, usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> obtenerEstadisticasPromocionCultura(String cuo, int anio, String usuario) {
        return procesarMeses(repoCultura.contarPorMes(anio, usuario));
    }

    private List<Integer> procesarMeses(List<Object[]> dataCruda) {
        Integer[] meses = new Integer[12];
        Arrays.fill(meses, 0);

        if (dataCruda != null) {
            for (Object[] fila : dataCruda) {
                if (fila[0] != null && fila[1] != null) {
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