package pe.gob.pj.prueba.infraestructure.rest.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.rest.requests.LoginRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.ObtenerOpcionesRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.ObtenerPerfilOpcionesResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ErrorResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.IniciarSesionResponse;

@RestController
@RequestMapping(value = "authenticate",
    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@Tag(name = "Acceso", description = "API para autenticación y gestión de accesos")
public interface Acceso {

  @PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Iniciar sesión", operationId = "iniciarSesion",
      description = "Permite a un usuario iniciar sesión en el sistema")
  @ApiResponse(responseCode = "200", description = "Sesión iniciada correctamente",
      content = @Content(schema = @Schema(implementation = IniciarSesionResponse.class)))
  @ApiResponse(responseCode = "401", description = "El cliente no se autentico de manera correcta",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "El cliente no esta autorizado para esta operación",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<IniciarSesionResponse> iniciarSesion(
      @Parameter(hidden = true) @RequestAttribute(
          name = ProjectConstants.PETICION) PeticionServicios peticion,
      @Valid @RequestBody LoginRequest login);

  @PostMapping(value = "opciones", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Obtener opciones del perfil", operationId = "obtenerOpciones",
      description = "Obtiene las opciones disponibles para un perfil específico")
  @ApiResponse(responseCode = "200", description = "Opciones obtenidas correctamente",
      content = @Content(schema = @Schema(implementation = ObtenerPerfilOpcionesResponse.class)))
  @ApiResponse(responseCode = "401", description = "El cliente no se autentico de manera correcta",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "El cliente no esta autorizado para esta operación",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<ObtenerPerfilOpcionesResponse> obtenerOpciones(
      @Parameter(hidden = true) @RequestAttribute(
          name = ProjectConstants.PETICION) PeticionServicios peticion,
      @Valid @RequestBody ObtenerOpcionesRequest perfil);
}
