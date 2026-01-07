package pe.gob.pj.prueba.infraestructure.rest.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.rest.responses.AplicativoResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.RefrescarTokenResponse;

@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@Tag(name = "Preestablecido", description = "API para manejar endpoints bases")
public interface Default extends GenerarHttpHeader {

  /**
   * Método que sirve para verificar versión actual del aplicativo
   * 
   * @param cuo Código único de log
   * @return Datos del aplicativo
   */
  @GetMapping(value = "/healthcheck")
  @Operation(summary = "Verifica Situación", description = "Permite verificar estado del servicio")
  @ApiResponse(responseCode = "200", description = "Verificación exitosa",
      content = @Content(schema = @Schema(implementation = AplicativoResponse.class)))
  public ResponseEntity<AplicativoResponse> healthcheck(
      @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
      @RequestParam(defaultValue = "json", required = false) String formatoRespuesta);

  /**
   * MÉTODO QUE GENERA NUEVO TOKEN A PARTIR DE TOKEN ANTERIOR
   * 
   * @param token es token antentior
   * @param ipRemota es la ip desde donde lo solicita
   * @param tokenAdmin es el token de la seccion administrador
   * @param validTokenAdmin indicador si necesitamos validar token del admin
   * @param cuo código único de log
   * @return un nuevo token
   */
  @GetMapping(value = "/seguridad/refresh")
  @Operation(summary = "Refrescar Token",
      description = "Permite refrescar un token con los mismos atributos")
  @ApiResponse(responseCode = "200", description = "Refresh de token exitoso",
      content = @Content(schema = @Schema(implementation = RefrescarTokenResponse.class)))
  public ResponseEntity<RefrescarTokenResponse> refreshToken(
      @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
      @RequestParam(defaultValue = "json", required = false) String formatoRespuesta,
      @RequestParam(required = true) String token);

}
