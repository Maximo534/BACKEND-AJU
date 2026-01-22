package pe.gob.pj.prueba.infraestructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.prueba.domain.model.negocio.Dashboard;
import pe.gob.pj.prueba.domain.port.usecase.negocio.DashboardUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.DashboardMapper;
import pe.gob.pj.prueba.infraestructure.rest.responses.DashboardResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/publico/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardUseCasePort useCase;
    private final DashboardMapper mapper;

    @GetMapping("/graficos")
    public ResponseEntity<GlobalResponse> obtenerGraficos(
            @RequestParam(required = false) Integer anio
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            int anioConsulta = (anio != null) ? anio : LocalDate.now().getYear();

            String usuarioFinal = "EMATAMOROSV";

            Dashboard dominio = useCase.obtenerDashboard(anioConsulta, usuarioFinal);

            DashboardResponse response = mapper.toResponse(dominio);

            res.setCodigo("200");
            res.setDescripcion("Dashboard cargado correctamente.");
            res.setData(response);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error obteniendo dashboard", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}