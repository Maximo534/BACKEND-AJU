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
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarDocumentoRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;

@RestController
@Validated
@RequestMapping(value = "/documentos", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "GestionDocumentos", description = "Módulo para la gestión de Guías, Memorias, Informes y Resoluciones")
public interface GestionDocumentos {

    @GetMapping
    @Operation(summary = "Listar Documentos por Tipo")
    ResponseEntity<GlobalResponse> listar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam(name = "tipo") String tipo
    );

    @GetMapping(value = "/{id}")
    @Operation(summary = "Obtener Detalle del Documento")
    ResponseEntity<GlobalResponse> obtenerPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar Nuevo Documento")
    ResponseEntity<GlobalResponse> registrar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarDocumentoRequest request,
            @RequestPart("archivo") MultipartFile archivo
    );

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar Documento (Archivo Opcional)")
    ResponseEntity<GlobalResponse> actualizar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarDocumentoRequest request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    );

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Eliminar Documento (Lógico)")
    ResponseEntity<GlobalResponse> eliminar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @GetMapping("/descargar/{id}")
    @Operation(summary = "Descargar Archivo Físico del Documento")
    ResponseEntity<Resource> descargar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );
}