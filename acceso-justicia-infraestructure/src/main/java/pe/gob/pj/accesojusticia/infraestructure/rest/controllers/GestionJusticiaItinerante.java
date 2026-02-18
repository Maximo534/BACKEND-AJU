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
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.ListarItineranteRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarFjiRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/justicia-itinerante", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "GestionJusticiaItinerante", description = "API para la gestión de registros de Justicia Itinerante")
public interface GestionJusticiaItinerante {

    @GetMapping
    @Operation(summary = "Listar Itinerantes")
    ResponseEntity<GlobalResponse> listar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @ModelAttribute ListarItineranteRequest filtros
    );


    @GetMapping(value = "/{id}")
    @Operation(summary = "Obtener Itinerante")
    ResponseEntity<GlobalResponse> obtenerPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar Itinerante")
    ResponseEntity<GlobalResponse> registrar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarFjiRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    );

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar Itinerante")
    ResponseEntity<GlobalResponse> actualizar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestPart("data") RegistrarFjiRequest request
    );

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

    @PostMapping(value = "/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agregar Archivo Extra")
    ResponseEntity<GlobalResponse> agregarArchivo(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam("idEvento") Long idEvento,
            @RequestParam("tipo") String tipo,
            @RequestPart("archivo") MultipartFile archivo
    );

    @DeleteMapping(value = "/archivo/{id}")
    @Operation(summary = "Eliminar Archivo")
    ResponseEntity<GlobalResponse> eliminarArchivo(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @GetMapping("/descargar/archivo/{id}")
    @Operation(summary = "Descargar Archivo Específico")
    ResponseEntity<Resource> descargarArchivoPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Long id
    );

    @GetMapping("/exportar-excel")
    @Operation(summary = "Exportar Listado a Excel")
    ResponseEntity<Resource> exportarExcel(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @ModelAttribute ListarItineranteRequest filtros
    );
}