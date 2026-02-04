package pe.gob.pj.prueba.usecase.negocio;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.EstadisticasData;
import pe.gob.pj.prueba.domain.port.persistence.negocio.EstadisticasPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.EstadisticasUseCasePort;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EstadisticasUseCaseAdapter implements EstadisticasUseCasePort {

    EstadisticasPersistencePort persistencePort;

    static final String TX_MANAGER = "txManagerNegocio";
    static final List<String> MESES_LABEL = List.of(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public EstadisticasData obtenerEstadisticasCompletas(String cuo, int anio) {

        // 1. Ejecutar las consultas SQL nativas
        List<Object[]> rawMagistrados = persistencePort.obtenerDataRanking(cuo, anio);
        List<Object[]> rawEjes = persistencePort.obtenerDataPorEje(cuo, anio);
        List<Object[]> rawResumen = persistencePort.obtenerResumenMagistrado(cuo, anio);
        List<Object[]> rawDistritos = persistencePort.obtenerDataDistritos(cuo, anio);
        List<Object[]> rawEvolucion = persistencePort.obtenerEvolucionMensual(cuo, anio);

        return EstadisticasData.builder()
                .anio(anio)
                .chartTopMagistrados(procesarGrafico(rawMagistrados))
                .chartPorEje(procesarGrafico(rawEjes))
                .chartResumenMagistrados(procesarResumenMagistrado(rawResumen))
                .chartTopDistrito(procesarGrafico(rawDistritos))
                .chartEvolucionMensual(procesarEvolucionMensual(rawEvolucion))
                .build();
    }

    // --- MÉTODOS PRIVADOS (Procesamiento de Datos) ---

    private EstadisticasData.DetalleGrafico procesarGrafico(List<Object[]> dataRaw) {
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
        return EstadisticasData.DetalleGrafico.builder().labels(labels).cantidad(values).build();
    }

    private EstadisticasData.ResumenMagistrado procesarResumenMagistrado(List<Object[]> dataRaw) {
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

        return EstadisticasData.ResumenMagistrado.builder()
                .labels(labels).dataJusticia(listJI).dataCultura(listCJ).dataFortalecimiento(listFC)
                .build();
    }

    private EstadisticasData.EvolucionMensual procesarEvolucionMensual(List<Object[]> dataRaw) {
        int[] ji = new int[12];
        int[] cj = new int[12];
        int[] fc = new int[12];

        if (dataRaw != null) {
            for (Object[] fila : dataRaw) {
                int mes = (fila[0] != null) ? ((Number) fila[0]).intValue() : 0;
                String tipo = (fila[1] != null) ? fila[1].toString() : "";
                int cantidad = (fila[2] != null) ? ((Number) fila[2]).intValue() : 0;

                if (mes >= 1 && mes <= 12) {
                    switch (tipo) {
                        case "Justicia Itinerante" -> ji[mes - 1] += cantidad;
                        case "Cultura Jurídica" -> cj[mes - 1] += cantidad;
                        case "Fortalecimiento" -> fc[mes - 1] += cantidad;
                    }
                }
            }
        }

        return EstadisticasData.EvolucionMensual.builder()
                .labels(MESES_LABEL)
                .dataJusticia(intArrayToList(ji))
                .dataCultura(intArrayToList(cj))
                .dataFortalecimiento(intArrayToList(fc))
                .build();
    }

    private List<Integer> intArrayToList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int i : arr) list.add(i);
        return list;
    }
}