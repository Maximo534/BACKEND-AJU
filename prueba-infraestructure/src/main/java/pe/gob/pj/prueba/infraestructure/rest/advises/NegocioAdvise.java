package pe.gob.pj.prueba.infraestructure.rest.advises;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.exceptions.general.RolUsuarioTokenNoPermitidoException;
import pe.gob.pj.prueba.domain.exceptions.negocio.CredencialesSinCoincidenciaException;
import pe.gob.pj.prueba.domain.exceptions.negocio.OpcionesNoAsignadadException;
import pe.gob.pj.prueba.domain.exceptions.negocio.PersonaYaExisteException;
import pe.gob.pj.prueba.domain.exceptions.negocio.TipoDocumentoNoExisteException;
import pe.gob.pj.prueba.domain.exceptions.negocio.UsuarioNoEsDePoderJudicialExcepcion;
import pe.gob.pj.prueba.domain.exceptions.negocio.UsuarioSinPerfilAsignadoException;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.infraestructure.common.enums.TipoError;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;


@ControllerAdvice
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NegocioAdvise extends DefaultAdvise {

  public NegocioAdvise(AuditarPeticionUseCasePort auditoriaGeneralUseCasePort,
      AuditoriaGeneralMapper auditoriaGeneralMapper, ObjectMapper objectMaper) {
    super(auditoriaGeneralUseCasePort, auditoriaGeneralMapper, objectMaper);
  }

  @ExceptionHandler({PersonaYaExisteException.class})
  ResponseEntity<GlobalResponse> handlePersonaYaExisteException(PersonaYaExisteException ex,
      WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.PERSONA_YA_REGISTRADA);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.PERSONA_YA_REGISTRADA, ex));
  }

  @ExceptionHandler({OpcionesNoAsignadadException.class})
  ResponseEntity<GlobalResponse> handleOpcionesNoAsignadadException(OpcionesNoAsignadadException ex,
      WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.OPCIONES_NOASIGNADAS);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.OPCIONES_NOASIGNADAS, ex));
  }

  @ExceptionHandler({UsuarioSinPerfilAsignadoException.class})
  ResponseEntity<GlobalResponse> handleUsuarioSinPerfilAsignadoException(
      UsuarioSinPerfilAsignadoException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.PERFIL_NO_ASIGNADO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.PERFIL_NO_ASIGNADO, ex));
  }

  @ExceptionHandler({CredencialesSinCoincidenciaException.class})
  ResponseEntity<GlobalResponse> handleCredencialesSinCoincidenciaException(
      CredencialesSinCoincidenciaException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.CREDENCIALES_INCORRECTAS);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.CREDENCIALES_INCORRECTAS, ex));
  }

  @ExceptionHandler({TipoDocumentoNoExisteException.class})
  ResponseEntity<GlobalResponse> handleTipoDocumentoNoExisteException(
      TipoDocumentoNoExisteException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.TIPO_DOCUMENTO_NO_EXISTE);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.TIPO_DOCUMENTO_NO_EXISTE, ex));
  }

  @ExceptionHandler({UsuarioNoEsDePoderJudicialExcepcion.class})
  ResponseEntity<GlobalResponse> handleUsuarioNoEsDePoderJudicialExcepcion(
      UsuarioNoEsDePoderJudicialExcepcion ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.USUARIO_NO_ES_DE_PODER_JUDICIAL);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.USUARIO_NO_ES_DE_PODER_JUDICIAL, ex));
  }

  @ExceptionHandler({RolUsuarioTokenNoPermitidoException.class})
  ResponseEntity<GlobalResponse> handleRolUsuarioTokenNoPermitidoException(
      RolUsuarioTokenNoPermitidoException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.NUEVO_TOKEN_NO_VALIDO);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.NUEVO_TOKEN_NO_VALIDO, ex));
  }


}
