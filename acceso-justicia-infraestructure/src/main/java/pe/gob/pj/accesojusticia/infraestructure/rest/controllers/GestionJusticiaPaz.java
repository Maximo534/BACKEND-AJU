package pe.gob.pj.accesojusticia.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.accesojusticia.domain.common.utils.ProjectConstants;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.ListarJpeCasosRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarCasoRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/justicia-paz", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "GestionJusticiaPaz", description = "API para la gestión de Casos Atendidos por Jueces de Paz Escolares")
public interface GestionJusticiaPaz {

    @GetMapping
    @Operation(summary = "Listar Casos Atendidos")
    ResponseEntity<GlobalResponse> listar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @ModelAttribute ListarJpeCasosRequest filtros
    );

    @GetMapping(value = "/{id}")
    @Operation(summary = "Obtener Caso por ID")
    ResponseEntity<GlobalResponse> obtenerPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar Caso de Justicia de Paz")
    ResponseEntity<GlobalResponse> registrar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarCasoRequest request,
            @RequestPart(value = "acta", required = false) MultipartFile acta,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    );

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar Caso de Justicia de Paz")
    ResponseEntity<GlobalResponse> actualizar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarCasoRequest request
    );

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener datos para el gráfico de barras por Corte")
    ResponseEntity<GlobalResponse> obtenerEstadisticasChart(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    @GetMapping("/ficha/{id}")
    @Operation(summary = "Descargar Ficha PDF del Caso")
    ResponseEntity<byte[]> descargarFichaPdf(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @GetMapping("/acta/{id}")
    @Operation(summary = "Descargar Acta del Caso")
    ResponseEntity<Resource> descargarActa(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @PostMapping(value = "/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agregar Archivo Extra (Ej: Fotos)")
    ResponseEntity<GlobalResponse> agregarArchivo(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam("idCaso") Long idCaso,
            @RequestParam("tipo") String tipo,
            @RequestPart("archivo") MultipartFile archivo
    );

    @DeleteMapping(value = "/archivo/{id}")
    @Operation(summary = "Eliminar Archivo Específico")
    ResponseEntity<GlobalResponse> eliminarArchivo(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @GetMapping("/descargar/archivo/{id}")
    @Operation(summary = "Descargar Archivo Específico por ID")
    ResponseEntity<Resource> descargarArchivoPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );
}