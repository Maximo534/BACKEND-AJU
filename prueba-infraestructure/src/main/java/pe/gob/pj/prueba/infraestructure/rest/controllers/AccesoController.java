package pe.gob.pj.prueba.infraestructure.rest.controllers;

import java.util.Arrays;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.query.IniciarSesionQuery;
import pe.gob.pj.prueba.domain.port.usecase.negocio.ConsultarOpcionesPerfilUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.IniciarSesionUseCasePort;
import pe.gob.pj.prueba.infraestructure.common.utils.JwtUtils;
import pe.gob.pj.prueba.infraestructure.mappers.OpcionMapper;
import pe.gob.pj.prueba.infraestructure.mappers.UsuarioMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.LoginRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.ObtenerOpcionesRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.IniciarSesionResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ObtenerPerfilOpcionesResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.PerfilUsuarioResponse;

@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccesoController implements Acceso, GenerarHttpHeader {

  IniciarSesionUseCasePort iniciarSesionUseCasePort;
  ConsultarOpcionesPerfilUseCasePort consultarOpcionesPerfilUseCasePort;
  UsuarioMapper usuarioMapper;
  OpcionMapper opcionMapper;
  JwtUtils jwtUtils;

  @Override
  public ResponseEntity<IniciarSesionResponse> iniciarSesion(PeticionServicios peticion,
      @Valid LoginRequest request) {

    var usuario =
        usuarioMapper.toUsuarioResponse(iniciarSesionUseCasePort.iniciarSesion(peticion.getCuo(),
            IniciarSesionQuery.builder().usuario(request.getUsuario()).clave(request.getClave())
                .aplicaCaptcha(request.getAplicaCaptcha()).tokenCaptcha(request.getTokenCaptcha())
                .build(),
            peticion));

    usuario.setToken(
        jwtUtils.generarNuevoToken(peticion.getCuo(), peticion.getJwt(), request.getUsuario(),
            usuario.getPerfiles().stream().map(PerfilUsuarioResponse::getRol).toList(),
            peticion.getIpPublica()));

    return ResponseEntity.ok().headers(getHttpHeader(request.getFormatoRespuesta()))
        .body(new IniciarSesionResponse(peticion.getCuo(), usuario));
  }

  @Override
  public ResponseEntity<ObtenerPerfilOpcionesResponse> obtenerOpciones(PeticionServicios peticion,
      @Valid ObtenerOpcionesRequest request) {

    var opciones = opcionMapper.toOpcionesPerfilResponse(consultarOpcionesPerfilUseCasePort
        .obtenerOpciones(peticion.getCuo(), request.getUsuario(), request.getIdPerfil(), peticion));

    opciones.setToken(jwtUtils.generarNuevoToken(peticion.getCuo(), peticion.getJwt(),
        request.getUsuario(), Arrays.asList(opciones.getRol()), peticion.getIpPublica()));

    return ResponseEntity.ok().headers(getHttpHeader(request.getFormatoRespuesta()))
        .body(new ObtenerPerfilOpcionesResponse(peticion.getCuo(), opciones));
  }

}
