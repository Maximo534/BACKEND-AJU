package pe.gob.pj.prueba.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

@RestController
@Validated
@RequestMapping(value = "/dashboard", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Dashboard", description = "Módulo de analítica y reportes gerenciales")
public interface GestionDashboard {

    @GetMapping
    @Operation(summary = "Obtener Dashboard Consolidado Anual")
    ResponseEntity<GlobalResponse> obtenerDashboard(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam(name = "anio", required = false) Integer anio
    );
}