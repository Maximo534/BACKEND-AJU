package pe.gob.pj.prueba.infraestructure.rest.controllers;

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
import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/buena-practica", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "GestionBuenaPractica", description = "API para la gestión del Registro de Buenas Prácticas")
public interface GestionBuenaPractica {

    @GetMapping
    @Operation(summary = "Listar Buenas Prácticas")
    ResponseEntity<GlobalResponse> listar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @ModelAttribute ListarBuenaPracticaRequest filtros
    );

    @GetMapping(value = "/{id}")
    @Operation(summary = "Obtener Buena Práctica por ID")
    ResponseEntity<GlobalResponse> obtenerPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar Buena Práctica")
    ResponseEntity<GlobalResponse> registrar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarBuenaPracticaRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "ppt", required = false) MultipartFile ppt,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    );

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar Buena Práctica")
    ResponseEntity<GlobalResponse> actualizar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarBuenaPracticaRequest request
    );

    // =========================================================================================
    // ESTADÍSTICAS / GRÁFICOS
    // =========================================================================================

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener datos para el gráfico de barras por Corte")
    ResponseEntity<GlobalResponse> obtenerEstadisticasChart(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );

    // =========================================================================================
    // ARCHIVOS ESPECÍFICOS
    // =========================================================================================

    @GetMapping("/ficha/{id}")
    @Operation(summary = "Descargar Ficha PDF")
    ResponseEntity<byte[]> descargarFichaPdf(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @GetMapping("/anexo/{id}")
    @Operation(summary = "Descargar Anexo Principal")
    ResponseEntity<Resource> descargarAnexo(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @GetMapping("/ppt/{id}")
    @Operation(summary = "Descargar Presentación PPT")
    ResponseEntity<Resource> descargarPpt(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    // =========================================================================================
    // ARCHIVOS GENERALES
    // =========================================================================================

    @PostMapping(value = "/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agregar Archivo Extra")
    ResponseEntity<GlobalResponse> agregarArchivo(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam("idEvento") Long idEvento,
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