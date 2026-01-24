package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.DetalleGrafico;
import pe.gob.pj.prueba.domain.model.negocio.EstadisticasData;
import pe.gob.pj.prueba.domain.model.negocio.EvolucionMensual;
import pe.gob.pj.prueba.domain.model.negocio.ResumenMagistrado;
import pe.gob.pj.prueba.domain.port.persistence.negocio.EstadisticasPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.EstadisticasUseCasePort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EstadisticasUseCaseAdapter implements EstadisticasUseCasePort {

    private final EstadisticasPersistencePort persistencePort;

    @Override
    @Transactional(readOnly = true)
    public EstadisticasData obtenerEstadisticasCompletas(int anio) throws Exception {

        List<Object[]> rawMagistrados = persistencePort.obtenerDataRanking(anio);
        List<Object[]> rawEjes = persistencePort.obtenerDataPorEje(anio);
        List<Object[]> rawResumen = persistencePort.obtenerResumenMagistrado(anio);
        List<Object[]> rawDistritos = persistencePort.obtenerDataDistritos(anio);
        List<Object[]> rawEvolucion = persistencePort.obtenerEvolucionMensual(anio);

        return EstadisticasData.builder()
                .anio(anio)
                .chartTopMagistrados(procesarGrafico(rawMagistrados))
                .chartPorEje(procesarGrafico(rawEjes))
                .chartResumenMagistrados(procesarResumenMagistrado(rawResumen))
                .chartTopDistrito(procesarGrafico(rawDistritos))
                .chartEvolucionMensual(procesarEvolucionMensual(rawEvolucion))
                .build();
    }

    private ResumenMagistrado procesarResumenMagistrado(List<Object[]> dataRaw) {
        Map<String, int[]> mapaUsuarios = new LinkedHashMap<>();

        if (dataRaw != null) {
            for (Object[] fila : dataRaw) {
                String usuario = (fila[0] != null) ? fila[0].toString() : "Desconocido";
                String tipo = (fila[1] != null) ? fila[1].toString() : "";
                int cantidad = (fila[2] != null) ? ((Number) fila[2]).intValue() : 0;

                mapaUsuarios.putIfAbsent(usuario, new int[]{0, 0, 0});

                int[] contadores = mapaUsuarios.get(usuario);
                switch (tipo) {
                    case "Justicia Itinerante" -> contadores[0] += cantidad;
                    case "Cultura Jurídica" -> contadores[1] += cantidad;
                    case "Fortalecimiento" -> contadores[2] += cantidad;
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<Integer> listJI = new ArrayList<>();
        List<Integer> listCJ = new ArrayList<>();
        List<Integer> listFC = new ArrayList<>();

        for (Map.Entry<String, int[]> entry : mapaUsuarios.entrySet()) {
            labels.add(entry.getKey());
            listJI.add(entry.getValue()[0]);
            listCJ.add(entry.getValue()[1]);
            listFC.add(entry.getValue()[2]);
        }

        // ✅ Construimos el objeto con el nombre correcto
        return ResumenMagistrado.builder()
                .labels(labels)
                .dataJusticia(listJI)
                .dataCultura(listCJ)
                .dataFortalecimiento(listFC)
                .build();
    }

    private EvolucionMensual procesarEvolucionMensual(List<Object[]> dataRaw) {
        // Inicializamos arrays de 12 ceros (uno por mes)
        int[] ji = new int[12];
        int[] cj = new int[12];
        int[] fc = new int[12];

        if (dataRaw != null) {
            for (Object[] fila : dataRaw) {
                int mes = (fila[0] != null) ? ((Number) fila[0]).intValue() : 0;
                String tipo = (fila[1] != null) ? fila[1].toString() : "";
                int cantidad = (fila[2] != null) ? ((Number) fila[2]).intValue() : 0;

                // Ajustamos índice (Mes 1 -> Array Index 0)
                if (mes >= 1 && mes <= 12) {
                    switch (tipo) {
                        case "Justicia Itinerante" -> ji[mes - 1] += cantidad;
                        case "Cultura Jurídica" -> cj[mes - 1] += cantidad;
                        case "Fortalecimiento" -> fc[mes - 1] += cantidad;
                    }
                }
            }
        }

        // Convertimos int[] a List<Integer>
        return EvolucionMensual.builder()
                .labels(MESES_LABEL)
                .dataJusticia(intArrayToList(ji))
                .dataCultura(intArrayToList(cj))
                .dataFortalecimiento(intArrayToList(fc))
                .build();
    }
    private static final List<String> MESES_LABEL = List.of(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );
    private List<Integer> intArrayToList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int i : arr) list.add(i);
        return list;
    }

    private DetalleGrafico procesarGrafico(List<Object[]> dataRaw) {
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        if (dataRaw != null) {
            for (Object[] fila : dataRaw) {
                String nombre = (fila[0] != null) ? fila[0].toString() : "Sin Nombre";
                int cantidad = (fila[1] != null) ? ((Number) fila[1]).intValue() : 0;
                labels.add(nombre);
                values.add(cantidad);
            }
        }
        return DetalleGrafico.builder().labels(labels).cantidad(values).build();
    }
}