package pe.gob.pj.prueba.infraestructure.common.utils;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
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
import pe.gob.pj.prueba.domain.exceptions.general.RolUsuarioTokenNoPermitidoException;
import pe.gob.pj.prueba.infraestructure.common.enums.JwtPropiedades;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JwtUtils {

  SeguridadProperty seguridadProperties;

  public String generarNuevoToken(String cuo, String token, String usuario,
      List<String> rolesUsuario, String ipRemota) {
    var signingKey = JwtPropiedades.FIRMA.getValor().getBytes();
    try {
      Jws<Claims> parsedToken = parseToken(token, signingKey);
      validateRole(cuo, usuario, rolesUsuario, parsedToken);
      return createNewToken(usuario, rolesUsuario, ipRemota, signingKey, parsedToken);
    } catch (ExpiredJwtException e) {
      return handleExpiredToken(usuario, rolesUsuario, ipRemota, signingKey, e);
    }
  }

  Jws<Claims> parseToken(String token, byte[] signingKey) {
    return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(signingKey)).build()
        .parseClaimsJws(token);
  }

  void validateRole(String cuo, String usuario, List<String> rolesUsuario,
      Jws<Claims> parsedToken) {
    var rolSeleccionado = rolesUsuario.get(0);
    @SuppressWarnings("unchecked")
    List<String> rolesToken =
        (List<String>) parsedToken.getBody().get(JwtPropiedades.CLAIM_ROLES.getNombre());
    if (rolesToken.stream().noneMatch(rolSeleccionado::equals)) {
      log.error(
          "{} Error al generar nuevo token, el rol seleccionado [{}] no es un rol asignado al usuario [{}].",
          cuo, rolSeleccionado, usuario);
      throw new RolUsuarioTokenNoPermitidoException();
    }
  }

  String createNewToken(String usuario, List<String> rolesUsuario, String ipRemota,
      byte[] signingKey, Jws<Claims> parsedToken) {
    var ahora = new Date();
    var tiempoSegundosExpira = seguridadProperties.getTokenExpira();
    var tiempoSegundosRefresh = seguridadProperties.getTokenRefresh();
    return Jwts.builder().signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
        .setHeaderParam(JwtPropiedades.CLAIM_TIPO.getNombre(), JwtPropiedades.CLAIM_TIPO.getValor())
        .setIssuer(JwtPropiedades.CLAIM_EMISOR.getValor())
        .setAudience(JwtPropiedades.CLAIM_DESTINO.getValor())
        .setSubject(parsedToken.getBody().getSubject())
        .setExpiration(ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira))
        .claim(JwtPropiedades.CLAIM_ROLES.getNombre(), rolesUsuario)
        .claim(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre(),
            parsedToken.getBody().get(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre()))
        .claim(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre(),
            parsedToken.getBody().get(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre()))
        .claim(JwtPropiedades.CLAIM_ROL_APLICATIVO.getNombre(), rolesUsuario.get(0))
        .claim(JwtPropiedades.CLAIM_USUARIO_APLICATIVO.getNombre(), usuario)
        .claim(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre(), ipRemota)
        .claim(JwtPropiedades.CLAIM_LIMITE_TOKEN.getNombre(),
            ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira + tiempoSegundosRefresh))
        .compact();
  }

  String handleExpiredToken(String usuario, List<String> rolesUsuario, String ipRemota,
      byte[] signingKey, ExpiredJwtException e) {
    var ahora = new Date();
    var tiempoSegundosExpira = seguridadProperties.getTokenExpira();
    var tiempoSegundosRefresh = seguridadProperties.getTokenRefresh();
    return Jwts.builder().signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
        .setHeaderParam(JwtPropiedades.CLAIM_TIPO.getNombre(), JwtPropiedades.CLAIM_TIPO.getValor())
        .setIssuer(JwtPropiedades.CLAIM_EMISOR.getValor())
        .setAudience(JwtPropiedades.CLAIM_DESTINO.getValor()).setSubject(e.getClaims().getSubject())
        .setExpiration(ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira))
        .claim(JwtPropiedades.CLAIM_ROLES.getNombre(), rolesUsuario)
        .claim(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre(),
            e.getClaims().get(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre()))
        .claim(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre(),
            e.getClaims().get(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre()))
        .claim(JwtPropiedades.CLAIM_ROL_APLICATIVO.getNombre(), rolesUsuario.get(0))
        .claim(JwtPropiedades.CLAIM_USUARIO_APLICATIVO.getNombre(), usuario)
        .claim(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre(), ipRemota)
        .claim(JwtPropiedades.CLAIM_LIMITE_TOKEN.getNombre(),
            ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira + tiempoSegundosRefresh))
        .compact();
  }

}
