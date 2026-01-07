package pe.gob.pj.prueba.infraestructure.rest.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.exceptions.general.IpTokenDifiereIpEndpointException;
import pe.gob.pj.prueba.domain.exceptions.general.TiempoActualizarTokenExcedidoException;
import pe.gob.pj.prueba.domain.model.AplicativoToken;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.common.enums.AplicativoInfo;
import pe.gob.pj.prueba.infraestructure.common.enums.JwtPropiedades;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;
import pe.gob.pj.prueba.infraestructure.rest.responses.AplicativoResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.RefrescarTokenResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultController implements Default {

  SeguridadProperty seguridadProperties;

  @GetMapping(value = "/healthcheck")
  public ResponseEntity<AplicativoResponse> healthcheck(PeticionServicios peticion,
      String formatoRespuesta) {

    var aplicativo = new HashMap<>();
    aplicativo.put("nombre", seguridadProperties.getAplicativoNombre());
    aplicativo.put(AplicativoInfo.VERSION_ACTUAL.getPropiedad(),
        AplicativoInfo.VERSION_ACTUAL.getNombre());

    return ResponseEntity.ok().headers(getHttpHeader(formatoRespuesta))
        .body(new AplicativoResponse(peticion.getCuo(), aplicativo));

  }

  @SuppressWarnings("unchecked")
  @GetMapping(value = "/seguridad/refresh")
  public ResponseEntity<RefrescarTokenResponse> refreshToken(PeticionServicios peticion,
      String formatoRespuesta, String token) {

    byte[] signingKey = JwtPropiedades.FIRMA.getValor().getBytes();

    var dataToken = new AplicativoToken();
    try {
      Jws<Claims> parsedToken = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(signingKey))
          .build().parseClaimsJws(token);
      List<String> roles =
          (List<String>) parsedToken.getBody().get(JwtPropiedades.CLAIM_ROLES.getNombre());
      var ipRemotaToken = parsedToken.getBody()
          .get(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre()).toString();
      var rolSeleccionado =
          parsedToken.getBody().get(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre()).toString();
      var usuario = parsedToken.getBody().get(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre())
          .toString();
      var subject = parsedToken.getBody().getSubject();

      var ahora = new Date();

      var tiempoSegundosExpira = seguridadProperties.getTokenExpira();
      var tiempoSegundosRefresh = seguridadProperties.getTokenRefresh();

      Date limiteExpira = parsedToken.getBody().getExpiration();
      Date limiteRefresh = ProjectUtils.sumarRestarSegundos(limiteExpira, tiempoSegundosRefresh);

      if (!peticion.getIpPublica().equals(ipRemotaToken)) {
        log.error(
            "{} La ip del token origen [{}] no coincide con la ip de la peticion actual [{}].",
            peticion.getCuo(), ipRemotaToken, peticion.getIp());
        throw new IpTokenDifiereIpEndpointException(String.format(
            "%s La ip del token origen [%s] no coincide con la ip de la peticion actual [%s]",
            peticion.getCuo(), ipRemotaToken, peticion.getIp()));
      }

      if (ahora.after(limiteRefresh)) {
        log.error(
            "{} El tiempo límite para refrescar el token enviado a expirado actual [{}] limite [{}].",
            peticion.getCuo(), ahora, limiteRefresh);
        throw new TiempoActualizarTokenExcedidoException(String.format(
            "El tiempo límite para refrescar el token enviado a expirado actual [%s] limite [%s].",
            ahora, limiteRefresh));
      }

      var tokenResult = Jwts.builder()
          .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
          .setHeaderParam(JwtPropiedades.CLAIM_TIPO.getNombre(),
              JwtPropiedades.CLAIM_TIPO.getValor())
          .setIssuer(JwtPropiedades.CLAIM_EMISOR.getValor())
          .setAudience(JwtPropiedades.CLAIM_DESTINO.getValor()).setSubject(subject)
          .setExpiration(ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira))
          .claim(JwtPropiedades.CLAIM_ROLES.getNombre(), roles)
          .claim(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre(), rolSeleccionado)
          .claim(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre(), usuario)
          .claim(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre(), peticion.getIp())
          .claim(JwtPropiedades.CLAIM_LIMITE_TOKEN.getNombre(),
              ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira + tiempoSegundosRefresh))
          .compact();
      dataToken.setToken(tokenResult);

    } catch (ExpiredJwtException e) {
      List<String> roles = (List<String>) e.getClaims().get(JwtPropiedades.CLAIM_ROLES.getNombre());
      var rolSeleccionado =
          e.getClaims().get(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre()).toString();
      var usuario =
          e.getClaims().get(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre()).toString();
      var ipRemotaToken =
          e.getClaims().get(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre()).toString();
      var subject = e.getClaims().getSubject();

      var ahora = new Date();

      var tiempoSegundosExpira = seguridadProperties.getTokenExpira();
      var tiempoSegundosRefresh = seguridadProperties.getTokenRefresh();

      var limiteExpira = e.getClaims().getExpiration();
      var limiteRefresh = ProjectUtils.sumarRestarSegundos(limiteExpira, tiempoSegundosRefresh);

      if (!peticion.getIpPublica().equals(ipRemotaToken)) {
        log.error(
            "{} La ip del token origen [{}] no coincide con la ip de la peticion actual [{}].",
            peticion.getCuo(), ipRemotaToken, peticion.getIp());
        throw new IpTokenDifiereIpEndpointException(String.format(
            "%s La ip del token origen [%s] no coincide con la ip de la peticion actual [%s]",
            peticion.getCuo(), ipRemotaToken, peticion.getIp()));
      }

      if (ahora.after(limiteRefresh)) {
        log.error(
            "{} El tiempo límite para refrescar el token enviado a expirado actual [{}] limite [{}].",
            peticion.getCuo(), ahora, limiteRefresh);
        throw new TiempoActualizarTokenExcedidoException(String.format(
            "El tiempo límite para refrescar el token enviado a expirado actual [%s] limite [%s].",
            ahora, limiteRefresh));
      }

      String tokenResult = Jwts.builder()
          .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
          .setHeaderParam(JwtPropiedades.CLAIM_TIPO.getNombre(),
              JwtPropiedades.CLAIM_TIPO.getValor())
          .setIssuer(JwtPropiedades.CLAIM_EMISOR.getValor())
          .setAudience(JwtPropiedades.CLAIM_DESTINO.getValor()).setSubject(subject)
          .setExpiration(ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira))
          .claim(JwtPropiedades.CLAIM_ROLES.getNombre(), roles)
          .claim(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre(), rolSeleccionado)
          .claim(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre(), usuario)
          .claim(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre(), peticion.getIp())
          .claim(JwtPropiedades.CLAIM_LIMITE_TOKEN.getNombre(),
              ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira + tiempoSegundosRefresh))
          .compact();
      dataToken.setToken(tokenResult);
    }
    return ResponseEntity.ok().headers(getHttpHeader(formatoRespuesta))
        .body(new RefrescarTokenResponse(peticion.getCuo(), dataToken));

  }

}
