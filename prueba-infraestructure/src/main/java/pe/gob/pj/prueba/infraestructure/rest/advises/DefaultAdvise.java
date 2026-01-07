package pe.gob.pj.prueba.infraestructure.rest.advises;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.exceptions.general.AuditoriaNoEncontradaException;
import pe.gob.pj.prueba.domain.exceptions.general.CantidadResultadoNoEsperadoException;
import pe.gob.pj.prueba.domain.exceptions.general.CaptchaException;
import pe.gob.pj.prueba.domain.exceptions.general.CargaArchivoAlfrescoFallidoException;
import pe.gob.pj.prueba.domain.exceptions.general.ClienteNoValidoException;
import pe.gob.pj.prueba.domain.exceptions.general.ConexionesDinamicasException;
import pe.gob.pj.prueba.domain.exceptions.general.CredencialesConexionDinamicaNoEncontradoException;
import pe.gob.pj.prueba.domain.exceptions.general.CredencialesConexionNoCoincidenException;
import pe.gob.pj.prueba.domain.exceptions.general.DescargaArchivoAlfrescoFallidoException;
import pe.gob.pj.prueba.domain.exceptions.general.EjecucionSpException;
import pe.gob.pj.prueba.domain.exceptions.general.EndpointNoSatisfactorioException;
import pe.gob.pj.prueba.domain.exceptions.general.IpTokenDifiereIpEndpointException;
import pe.gob.pj.prueba.domain.exceptions.general.NoSePuedeDesencriptarClaveException;
import pe.gob.pj.prueba.domain.exceptions.general.ParametrosConsumoNoDesencriptadosException;
import pe.gob.pj.prueba.domain.exceptions.general.TiempoActualizarTokenExcedidoException;
import pe.gob.pj.prueba.domain.exceptions.general.TokenExpiradoException;
import pe.gob.pj.prueba.domain.exceptions.general.TokenNoValidoException;
import pe.gob.pj.prueba.domain.exceptions.general.UsuarioRolNoTienePermisosException;
import pe.gob.pj.prueba.domain.exceptions.general.UsuarioTokenNoValidoException;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.infraestructure.common.enums.TipoError;
import pe.gob.pj.prueba.infraestructure.common.utils.ManejoExcepcionUtils;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.rest.controllers.MonitorearRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class DefaultAdvise extends ResponseEntityExceptionHandler
    implements MonitorearRequest {

  @Getter
  AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
  @Getter
  private final AuditoriaGeneralMapper auditoriaGeneralMapper;
  @Getter
  private final ObjectMapper objectMaper;

  static final String RESPONSE_CODIGO = "codigo";
  static final String RESPONSE_DESCRIPCION = "descripcion";
  static final String RESPONSE_CUO = "codigoOperacion";
  static final String RESPONSE_DATA = "data";

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status,
      WebRequest request) {

    var cuo = obtenerCuo(request);
    Map<String, Object> body = new LinkedHashMap<>();
    List<String> errorList = new ArrayList<>();
    errorList.add(
        String.format("El parámetro %s es obligatorio y no está presente.", ex.getParameterName()));

    body.put(RESPONSE_CODIGO, TipoError.PARAMETROS_NO_VALIDOS.getCodigo());
    body.put(RESPONSE_DESCRIPCION, TipoError.PARAMETROS_NO_VALIDOS.getDescripcionUsuario());
    body.put(RESPONSE_CUO, cuo);
    body.put(RESPONSE_DATA, errorList);

    log.error("{} - {}", cuo, String.join(", ", errorList));
    var peticion = obtenerPeticionServicio(request, TipoError.PARAMETROS_NO_VALIDOS);
    guardarAuditoria(peticion);
    return ResponseEntity.status(HttpStatus.OK).body(body);
  }

  // valid
  @ExceptionHandler(value = {ConstraintViolationException.class})
  protected ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {

    var cuo = obtenerCuo(request);
    Map<String, Object> body = new LinkedHashMap<>();
    List<String> errorList = new ArrayList<>();
    ex.getConstraintViolations()
        .forEach(violation -> errorList.add(String.format("%s: %s",
            Optional.ofNullable(violation.getInvalidValue()).map(Object::toString).orElse(null),
            violation.getMessage())));
    body.put(RESPONSE_CODIGO, TipoError.PARAMETROS_NO_VALIDOS.getCodigo());
    body.put(RESPONSE_DESCRIPCION, TipoError.PARAMETROS_NO_VALIDOS.getDescripcionUsuario());
    body.put(RESPONSE_CUO, cuo);
    body.put(RESPONSE_DATA, errorList);

    log.error("{} - {}", cuo, String.join(", ", errorList));
    var peticion = obtenerPeticionServicio(request, TipoError.PARAMETROS_NO_VALIDOS);
    guardarAuditoria(peticion);
    return ResponseEntity.status(HttpStatus.OK).body(body);
  }

  // validated
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {

    var cuo = obtenerCuo(request);
    Map<String, Object> body = new LinkedHashMap<>();
    var errorList = new ArrayList<String>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      if (error instanceof FieldError fieldError) {
        errorList.add(String.format("%s: %s",
            Optional.ofNullable(fieldError.getRejectedValue()).map(Object::toString).orElse(null),
            fieldError.getDefaultMessage()));
      }
    });
    body.put(RESPONSE_CODIGO, TipoError.PARAMETROS_NO_VALIDOS.getCodigo());
    body.put(RESPONSE_DESCRIPCION, TipoError.PARAMETROS_NO_VALIDOS.getDescripcionUsuario());
    body.put(RESPONSE_CUO, cuo);
    body.put(RESPONSE_DATA, errorList);

    log.error("{} {} ", cuo, String.join(", ", errorList));
    var peticion = obtenerPeticionServicio(request, TipoError.PARAMETROS_NO_VALIDOS);
    guardarAuditoria(peticion);
    return new ResponseEntity<>(body, headers, HttpStatus.OK);

  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status,
      WebRequest request) {

    var cuo = obtenerCuo(request);
    Map<String, Object> body = new LinkedHashMap<>();

    StringBuilder builder = new StringBuilder();
    if (!ProjectUtils.isNullOrEmpty(ex.getContentType())) {
      builder.append("El formato de solicitud ");
      builder.append(ex.getContentType());
      builder.append(" no es compatible.");
    } else {
      builder
          .append("No se mandaron los datos requeridos relacionados al cuerpo de la solicitud. ");
    }

    List<MediaType> mediaTypes = ex.getSupportedMediaTypes();

    body.put(RESPONSE_CODIGO, status.value());
    body.put(RESPONSE_DESCRIPCION, builder.toString());
    body.put(RESPONSE_CUO, cuo);
    body.put(RESPONSE_DATA, null);
    log.error("{} {} La URI: {} solo acepta formato {}", cuo, builder.toString(),
        ((ServletWebRequest) request).getRequest().getRequestURI(), mediaTypes);
    return new ResponseEntity<>(body, headers, HttpStatus.OK);// 415 -
                                                              // HttpStatus.UNSUPPORTED_MEDIA_TYPE
  }

  @ExceptionHandler({Exception.class})
  ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.ERROR_INESPERADO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.ERROR_INESPERADO, ex));
  }

  @ExceptionHandler({ParametrosConsumoNoDesencriptadosException.class})
  ResponseEntity<Object> handleParametrosConsumoNoDesencriptadosException(
      ParametrosConsumoNoDesencriptadosException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.PARAMETROS_CONSUMO_NO_DESENCRIPTADOS);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.PARAMETROS_CONSUMO_NO_DESENCRIPTADOS, ex));
  }

  @ExceptionHandler({ClienteNoValidoException.class})
  ResponseEntity<Object> handleClienteNoValidoException(ClienteNoValidoException ex,
      WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.PARAMETROS_CONSUMO_NO_VALIDOS);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.PARAMETROS_CONSUMO_NO_VALIDOS, ex));
  }

  @ExceptionHandler({AuditoriaNoEncontradaException.class})
  ResponseEntity<GlobalResponse> handleAuditoriaNoEncontradaException(
      AuditoriaNoEncontradaException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.AUDITORIA_PETICION_REQUERIDA);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.AUDITORIA_PETICION_REQUERIDA, ex));
  }

  @ExceptionHandler({TokenNoValidoException.class})
  ResponseEntity<GlobalResponse> handleTokenNoValidoException(TokenNoValidoException ex,
      WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.TOKEN_NO_VALIDO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.TOKEN_NO_VALIDO, ex));
  }

  @ExceptionHandler({UsuarioTokenNoValidoException.class})
  ResponseEntity<GlobalResponse> handleUsuarioTokenNoValidoException(
      UsuarioTokenNoValidoException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.USUARIO_TOKEN_NO_VALIDO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.USUARIO_TOKEN_NO_VALIDO, ex));
  }

  @ExceptionHandler({UsuarioRolNoTienePermisosException.class})
  ResponseEntity<GlobalResponse> handleUsuarioRolNoTienePermisosException(
      UsuarioRolNoTienePermisosException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.USUARIO_ROL_NO_TIENE_PERMISOS);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.USUARIO_ROL_NO_TIENE_PERMISOS, ex));
  }

  @ExceptionHandler({IpTokenDifiereIpEndpointException.class})
  ResponseEntity<GlobalResponse> handleIpTokenDifiereIpEndpointException(
      IpTokenDifiereIpEndpointException ex, WebRequest request) {
    var peticion =
        obtenerPeticionServicio(request, TipoError.TOKEN_NO_FUE_GENERADO_EN_SERVIDOR_DE_VALIDACION);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(
        handleResponse(peticion, TipoError.TOKEN_NO_FUE_GENERADO_EN_SERVIDOR_DE_VALIDACION, ex));
  }

  @ExceptionHandler({TiempoActualizarTokenExcedidoException.class})
  ResponseEntity<GlobalResponse> handleTiempoActualizarTokenExcedidoException(
      TiempoActualizarTokenExcedidoException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.TIEMPO_REFRESH_TOKEN_EXCEDIDO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.TIEMPO_REFRESH_TOKEN_EXCEDIDO, ex));
  }

  @ExceptionHandler({TokenExpiradoException.class})
  ResponseEntity<GlobalResponse> handleTokenExpiradoException(TokenExpiradoException ex,
      WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.TOKEN_EXPIRADO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.TOKEN_EXPIRADO, ex));
  }

  @ExceptionHandler({EndpointNoSatisfactorioException.class})
  ResponseEntity<GlobalResponse> handleEndpointNoSatisfactorioException(
      EndpointNoSatisfactorioException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.ENDPOINT_CONSUMIDO_FALLIDO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.ENDPOINT_CONSUMIDO_FALLIDO, ex));
  }

  @ExceptionHandler({ConexionesDinamicasException.class})
  ResponseEntity<GlobalResponse> handleConexionesDinamicasException(ConexionesDinamicasException ex,
      WebRequest request) {
    var peticion =
        obtenerPeticionServicio(request, TipoError.OBTENER_CREDENCIALES_CONEXIONES_FALLIDO);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.OBTENER_CREDENCIALES_CONEXIONES_FALLIDO, ex));
  }

  @ExceptionHandler({CredencialesConexionDinamicaNoEncontradoException.class})
  ResponseEntity<GlobalResponse> handleCredencialesConexionDinamicaNoEncontradoException(
      CredencialesConexionDinamicaNoEncontradoException ex, WebRequest request) {
    var peticion =
        obtenerPeticionServicio(request, TipoError.CORTE_NO_TIENE_CONEXIONES_CONFIGURADAS);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.CORTE_NO_TIENE_CONEXIONES_CONFIGURADAS, ex));
  }

  @ExceptionHandler({NoSePuedeDesencriptarClaveException.class})
  ResponseEntity<GlobalResponse> handleNoSePuedeDesencriptarClaveException(
      NoSePuedeDesencriptarClaveException ex, WebRequest request) {
    var peticion =
        obtenerPeticionServicio(request, TipoError.DESENCRIPTACION_CREDENCIAL_CONEXION_DINAMICA_FALLIDA);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.DESENCRIPTACION_CREDENCIAL_CONEXION_DINAMICA_FALLIDA, ex));
  }

  @ExceptionHandler({CredencialesConexionNoCoincidenException.class})
  ResponseEntity<GlobalResponse> handleCredencialesConexionNoCoincidenException(
      CredencialesConexionNoCoincidenException ex, WebRequest request) {
    var peticion =
        obtenerPeticionServicio(request, TipoError.CREDENCIALES_CONEXION_DINAMICA_ERRADAS);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.CREDENCIALES_CONEXION_DINAMICA_ERRADAS, ex));
  }

  @ExceptionHandler({CargaArchivoAlfrescoFallidoException.class})
  ResponseEntity<GlobalResponse> handleNoSeSubioArchivoAlfrescoException(
      CargaArchivoAlfrescoFallidoException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.CARGA_ARCHIVO_ALFRESCO_FALLIDO);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.CARGA_ARCHIVO_ALFRESCO_FALLIDO, ex));
  }

  @ExceptionHandler({DescargaArchivoAlfrescoFallidoException.class})
  ResponseEntity<GlobalResponse> handleDescargaArchivoAlfrescoFallidoException(
      DescargaArchivoAlfrescoFallidoException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.DESCARGA_ARCHIVO_ALFRESCO_FALLIDO);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.DESCARGA_ARCHIVO_ALFRESCO_FALLIDO, ex));
  }

  @ExceptionHandler({CaptchaException.class})
  ResponseEntity<GlobalResponse> handleCaptchaException(CaptchaException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.CAPTCHA_NO_VALIDADA);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.CAPTCHA_NO_VALIDADA, ex));
  }

  @ExceptionHandler({EjecucionSpException.class})
  ResponseEntity<GlobalResponse> handleEjecucionSpException(EjecucionSpException ex,
      WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.EJECUCION_SP_FALLIDO);
    guardarAuditoria(peticion);
    return ResponseEntity.ok(handleResponse(peticion, TipoError.EJECUCION_SP_FALLIDO, ex));
  }

  @ExceptionHandler({CantidadResultadoNoEsperadoException.class})
  ResponseEntity<GlobalResponse> handleTokenExpiradoException(
      CantidadResultadoNoEsperadoException ex, WebRequest request) {
    var peticion = obtenerPeticionServicio(request, TipoError.CANTIDAD_RESULTADO_NO_PERMITIDO);
    guardarAuditoria(peticion);
    return ResponseEntity
        .ok(handleResponse(peticion, TipoError.CANTIDAD_RESULTADO_NO_PERMITIDO, ex));
  }

  protected String obtenerCuo(WebRequest request) {
    return Optional.ofNullable((PeticionServicios) request.getAttribute(ProjectConstants.PETICION,
        RequestAttributes.SCOPE_REQUEST)).map(PeticionServicios::getCuo).orElse(null);
  }

  protected Optional<PeticionServicios> obtenerPeticionServicio(WebRequest request,
      TipoError tipoError) {
    var peticion = Optional.ofNullable((PeticionServicios) request
        .getAttribute(ProjectConstants.PETICION, RequestAttributes.SCOPE_REQUEST));
    peticion.ifPresent(p -> {
      p.setCodigoRespuesta(tipoError.getCodigo());
      p.setDescripcionRespuesta(tipoError.getDescripcionUsuario());
    });
    return peticion;
  }

  protected GlobalResponse handleResponse(Optional<PeticionServicios> peticion, TipoError tipoError,
      Exception excepcion) {
    var response = new GlobalResponse(peticion.map(PeticionServicios::getCuo).orElse(null));
    response.setCodigo(tipoError.getCodigo());
    response
        .setDescripcion(String.format(tipoError.getDescripcionUsuario(), excepcion.getMessage()));
    ManejoExcepcionUtils.handleException(response.getCodigoOperacion(), excepcion, tipoError);
    return response;
  }

}
