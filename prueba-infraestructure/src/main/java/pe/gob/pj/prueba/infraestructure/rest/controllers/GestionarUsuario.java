package pe.gob.pj.prueba.infraestructure.rest.controllers;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.rest.requests.CambiarClaveRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

@RestController
@Validated
@RequestMapping(value = "/usuarios", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@Tag(name = "GestionarUsuario", description = "API para administrar registros de usuarios")
public interface GestionarUsuario {

    /***
     * GET /usuarios : Listar usuarios
     */
    @GetMapping
    @Operation(summary = "Listar Usuarios", operationId = "listar", description = "Permite listar usuarios por filtros paginados")
    @ApiResponse(responseCode = "200", description = "Consulta realizada", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> listar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @Parameter(description = "Filtros de búsqueda") @ModelAttribute ListarUsuarioRequest filtros);

    /***
     * GET /usuarios/{id} : Obtener usuario por ID
     */
    @GetMapping(value = "/{id}")
    @Operation(summary = "Obtener Usuario", operationId = "obtenerPorId", description = "Obtiene el detalle de un usuario por su ID")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> obtenerPorId(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @PathVariable Integer id);

    /***
     * POST /usuarios : Registrar (Multipart)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // CAMBIO: Multipart
    @Operation(summary = "Registrar Usuario", operationId = "registrar", description = "Permite registrar un nuevo usuario")
    @ApiResponse(responseCode = "200", description = "Registro exitoso", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> registrar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            // CAMBIO: @RequestPart("data")
            @Parameter(description = "Datos del usuario en JSON string", schema = @Schema(implementation = RegistrarUsuarioRequest.class))
            @Valid @RequestPart("data") RegistrarUsuarioRequest request);

    /***
     * PUT /usuarios : Actualizar usuario (El ID viene dentro del JSON 'data')
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar Usuario", operationId = "actualizar", description = "Permite actualizar datos de un usuario")
    @ApiResponse(responseCode = "200", description = "Actualización correcta", content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> actualizar(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Parameter(description = "Datos del usuario a actualizar (Debe incluir ID)")
            @Valid @RequestPart("data") RegistrarUsuarioRequest request);

    @GetMapping(value = "/existe")
    @Operation(summary = "Verificar Disponibilidad de Login", operationId = "verificarLogin",
            description = "Verifica si un nombre de usuario (login) ya está registrado en el sistema.")
    @ApiResponse(responseCode = "200", description = "Verificación exitosa",
            content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> verificarLogin(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Parameter(description = "El login a verificar", required = true, example = "jperez")
            @RequestParam("login") @NotBlank String login);


    /***
     * PATCH /usuarios/{id}/estado : Activar o Desactivar usuario
     */
    @PostMapping(value = "/desactivar/{id}")
    @Operation(summary = "Cambiar Estado de Usuario", operationId = "cambiarEstado",
            description = "Permite activar ('1') o desactivar ('0') un usuario existente.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente",
            content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> cambiarEstado(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Parameter(description = "ID del usuario", required = true) @PathVariable Integer id,
            @Parameter(description = "Nuevo estado ('1' = Activo, '0' = Inactivo)", required = true, example = "1")
            @RequestParam("activo") String activo);

    @PostMapping(value = "/resetear-clave/{id}")
    @Operation(summary = "Resetear Contraseña", operationId = "resetearClave",
            description = "Resetea la contraseña de un usuario al valor por defecto. Requiere rol SYSADMIN o jerarquía superior.")
    @ApiResponse(responseCode = "200", description = "Contraseña reseteada correctamente",
            content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> resetearClave(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Parameter(description = "ID del usuario objetivo", required = true) @PathVariable Integer id);

    @PostMapping(value = "/cambiar-contrasena")
    @Operation(summary = "Cambiar Contraseña Propia", operationId = "cambiarContrasena",
            description = "Permite al usuario logueado cambiar su propia contraseña.")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente",
            content = @Content(schema = @Schema(implementation = GlobalResponse.class)))
    public ResponseEntity<GlobalResponse> cambiarContrasena(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion,
            @Valid @RequestBody CambiarClaveRequest request);

    @GetMapping(value = "/datos-sesion")
    @Operation(summary = "Obtener Datos de Sesión", description = "Devuelve nombre, cargo (con sigla), sede y eje del usuario actual.")
    ResponseEntity<GlobalResponse> obtenerPerfilSesion(
            @Parameter(hidden = true) @RequestAttribute(name = ProjectConstants.PETICION) PeticionServicios peticion
    );
}