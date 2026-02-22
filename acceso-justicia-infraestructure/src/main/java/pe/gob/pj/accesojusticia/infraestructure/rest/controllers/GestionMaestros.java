package pe.gob.pj.accesojusticia.infraestructure.rest.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.pj.accesojusticia.domain.common.utils.ProjectConstants;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;

@RestController
@Validated
@RequestMapping(value = "/maestros", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "GestionMaestros", description = "API para consultar catálogos y maestros del sistema")
public interface GestionMaestros {

    // --- PLANIFICACIÓN ---
    @GetMapping("/actividades-operativas")
    @Operation(summary = "Listar Actividades Operativas")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarActividades(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/indicadores/{idActividad}")
    @Operation(summary = "Listar Indicadores por Actividad")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarIndicadores(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long idActividad
    );

    @GetMapping("/tareas/{idIndicador}")
    @Operation(summary = "Listar Tareas por Indicador")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarTareas(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long idIndicador
    );

    @GetMapping("/distritos-judiciales/{id}")
    @Operation(summary = "Obtener Distrito Judicial por ID")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> obtenerDistritoJudicialPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    // --- ORGANIZACIÓN JUDICIAL ---
    @GetMapping("/distritos-judiciales")
    @Operation(summary = "Listar Distritos Judiciales")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarDistritosJudiciales(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/sedes/{idCorte}")
    @Operation(summary = "Listar Sedes por Corte")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarSedesPorCorte(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long idCorte
    );

    @GetMapping("/instancias/{idSede}")
    @Operation(summary = "Listar Instancias por Sede")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarInstanciasPorSede(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long idSede
    );

    // --- MAESTROS GENERALES ---
    @GetMapping("/ejes")
    @Operation(summary = "Listar Ejes")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarEjes(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/materias")
    @Operation(summary = "Listar Materias")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarMaterias(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/tipos-vulnerabilidad")
    @Operation(summary = "Listar Tipos de Vulnerabilidad")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarVulnerabilidades(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/tambos/{idCorte}")
    @Operation(summary = "Listar Tambos por Corte")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarTambos(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long idCorte
    );

    @GetMapping("/planes")
    @Operation(summary = "Buscar Planes Anuales")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> buscarPlanes(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam Long idCorte,
            @RequestParam String periodo
    );

    // --- UBIGEO ---
    @GetMapping("/ubigeo/departamentos")
    @Operation(summary = "Listar Departamentos")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarDepartamentos(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/ubigeo/provincias/{idDepartamento}")
    @Operation(summary = "Listar Provincias")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarProvincias(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long idDepartamento
    );

    @GetMapping("/ubigeo/distritos/{idProvincia}")
    @Operation(summary = "Listar Distritos")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarDistritos(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long idProvincia
    );

    // --- PARTICIPANTES ---
    @GetMapping("/tipos-participantes")
    @Operation(summary = "Listar Tipos de Participantes")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarTiposParticipantes(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/perfiles")
    @Operation(summary = "Listar Perfiles Disponibles", description = "Lista los perfiles que el usuario logueado tiene permiso de asignar.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    ResponseEntity<GlobalResponse> listarPerfiles(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );
}