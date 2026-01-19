package pe.gob.pj.prueba.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GenerarDescargaMasivaUseCasePort;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

@Slf4j
@RestController
@RequestMapping("/publico/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes y Descargas", description = "Módulo para generación de reportes y descargas masivas")
public class ReportesController {

    private final GenerarDescargaMasivaUseCasePort descargaMasivaUseCase;

    @Operation(summary = "Descarga Masiva de Evidencias (ZIP)",
            description = "Genera un archivo ZIP con las evidencias filtradas por módulo, año y mes.")
    @ApiResponse(responseCode = "200", description = "Archivo ZIP generado exitosamente",
            content = @Content(mediaType = "application/octet-stream"))
    @ApiResponse(responseCode = "500", description = "Error interno o sin archivos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class)))
    @GetMapping("/descarga-masiva")
    public ResponseEntity<?> descargarMasivo(
            @RequestParam("modulo") String modulo,        // Ej: evidencias_fji
            @RequestParam("tipoArchivo") String tipoArchivo, // Ej: FOTO, ANEXO
            @RequestParam("anio") Integer anio,
            @RequestParam("mes") Integer mes
    ) {
        try {
            log.info("Solicitando descarga masiva: Modulo={}, Tipo={}, Periodo={}-{}", modulo, tipoArchivo, mes, anio);

            // Llamada al caso de uso a través del puerto
            RecursoArchivo recurso = descargaMasivaUseCase.generarZipMasivo(modulo, tipoArchivo, anio, mes);

            // Respuesta Exitosa: Stream del ZIP
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getNombreFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // Indica que es un archivo binario
                    .body(new InputStreamResource(recurso.getStream()));

        } catch (Exception e) {
            log.error("Error en descarga masiva", e);

            // Respuesta de Error: JSON (GlobalResponse)
            // El frontend debe detectar si el 'content-type' es application/json para mostrar el mensaje de error
            GlobalResponse res = new GlobalResponse();
            res.setCodigo("500");
            res.setDescripcion("Error generando reporte: " + e.getMessage());

            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(res);
        }
    }
}