package pe.gob.pj.accesojusticia.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.accesojusticia.domain.common.utils.ProjectConstants;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;

@RestController
@Validated
@RequestMapping(value = "/estadisticas", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Estadisticas", description = "Reportes gerenciales globales del sistema")
public interface GestionEstadisticas {

    @GetMapping
    @Operation(summary = "Obtener Estadísticas Globales del Sistema (Magistrados, Ejes, Evolución)")
    ResponseEntity<GlobalResponse> obtenerEstadisticas(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam(name = "anio", required = false) Integer anio
    );
}