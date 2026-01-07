package pe.gob.pj.prueba.infraestructure.rest.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.common.utils.PatronValidacionConstants;
import pe.gob.pj.prueba.infraestructure.rest.requests.PersonaRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.ConsultaPersonaResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ErrorResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.IdentificadorPersonaResponse;

@RestController
@Validated
@RequestMapping(value = "personas",
    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@Tag(name = "GestionPersona", description = "API para administrar registros de personas")
public interface GestionPersona {

  /***
   * 
   * GET /personas : Consultar datos de persona
   * 
   * @param peticion
   * @param formatoRespuesta
   * @param numeroDocumento
   * @return
   */
  @GetMapping
  @Operation(summary = "Consultar Persona", operationId = "consultarPersonas",
      description = "Permite consultar personas por filtro")
  @ApiResponse(responseCode = "200", description = "Consulta realizada",
      content = @Content(schema = @Schema(implementation = ConsultaPersonaResponse.class)))
  @ApiResponse(responseCode = "401", description = "El cliente no se autentico de manera correcta",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "El cliente no esta autorizado para esta operación",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<ConsultaPersonaResponse> consultarPersonas(
      @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
      @RequestParam(defaultValue = "json", required = false) String formatoRespuesta,
      @Size(min = 8, max = 8,
          message = "El parámetro numero_documento tiene un tamaño no valido [min1,max=1].") @Pattern(
              regexp = PatronValidacionConstants.NUMBER,
              message = "El parámetro numero_documento solo permite valores numéricos.") @RequestParam(
                  name = "numero_documento", required = false) String numeroDocumento);

  /***
   * 
   * POST /personas : Crear persona en base a los datos enviados
   * 
   * @param peticion
   * @param persona
   * @return
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Registrar Persona", operationId = "registrarPersona",
      description = "Permite registrar persona")
  @ApiResponse(responseCode = "200", description = "Registro exitoso",
      content = @Content(schema = @Schema(implementation = IdentificadorPersonaResponse.class)))
  @ApiResponse(responseCode = "401", description = "El cliente no se autentico de manera correcta",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "El cliente no esta autorizado para esta operación",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<IdentificadorPersonaResponse> registrarPersona(
      @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
      @Validated @RequestBody PersonaRequest persona);

  /***
   * 
   * PUT /personas/actualizar/{id} : Actualizar persona en base al id
   * 
   * @param peticion
   * @param id
   * @param persona
   * @return
   */
  @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Actualizar Persona", operationId = "actualizarPersona",
      description = "Permite actualizar datos de una persona")
  @ApiResponse(responseCode = "200", description = "Actualización correcta",
      content = @Content(schema = @Schema(implementation = IdentificadorPersonaResponse.class)))
  @ApiResponse(responseCode = "401", description = "El cliente no se autentico de manera correcta",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "El cliente no esta autorizado para esta operación",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<IdentificadorPersonaResponse> actualizarPersona(
      @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
      @PathVariable Integer id, @Valid @RequestBody PersonaRequest persona);

}
