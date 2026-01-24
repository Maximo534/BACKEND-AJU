package pe.gob.pj.prueba.infraestructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.prueba.domain.model.negocio.EstadisticasData;
import pe.gob.pj.prueba.domain.port.usecase.negocio.EstadisticasUseCasePort;
import pe.gob.pj.prueba.infraestructure.rest.responses.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/publico/v1/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EstadisticasUseCasePort useCase;

    @GetMapping
    public ResponseEntity<GlobalResponse> obtenerEstadisticas(
            @RequestParam(required = false) Integer anio
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            int anioConsulta = (anio != null) ? anio : LocalDate.now().getYear();

            EstadisticasData dominio = useCase.obtenerEstadisticasCompletas(anioConsulta);

            EstadisticasResponse response = EstadisticasResponse.builder()
                    .anioConsultado(anioConsulta)
                    .chartTopMagistrados(GraficoResponse.builder()
                            .labels(dominio.getChartTopMagistrados().getLabels())
                            .cantidad(dominio.getChartTopMagistrados().getCantidad())
                            .build())
                    .chartPorEje(GraficoResponse.builder()
                            .labels(dominio.getChartPorEje().getLabels())
                            .cantidad(dominio.getChartPorEje().getCantidad())
                            .build())
                    .chartResumenMagistrados(GraficoResumenMagistradoResponse.builder()
                            .labels(dominio.getChartResumenMagistrados().getLabels())
                            .dataJusticia(dominio.getChartResumenMagistrados().getDataJusticia())
                            .dataCultura(dominio.getChartResumenMagistrados().getDataCultura())
                            .dataFortalecimiento(dominio.getChartResumenMagistrados().getDataFortalecimiento())
                            .build())
                    .chartTopDistrito(GraficoResponse.builder()
                            .labels(dominio.getChartTopDistrito().getLabels())
                            .cantidad(dominio.getChartTopDistrito().getCantidad())
                            .build())
                    .chartEvolucionMensual(GraficoEvolucionMensualResponse.builder()
                            .labels(dominio.getChartEvolucionMensual().getLabels())
                            .dataJusticia(dominio.getChartEvolucionMensual().getDataJusticia())
                            .dataCultura(dominio.getChartEvolucionMensual().getDataCultura())
                            .dataFortalecimiento(dominio.getChartEvolucionMensual().getDataFortalecimiento())
                            .build())
                    .build();;

            res.setCodigo("200");
            res.setDescripcion("Estadísticas cargadas correctamente.");
            res.setData(response);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error obteniendo estadísticas", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}