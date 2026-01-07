package pe.gob.pj.prueba.infraestructure.security.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.exceptions.general.ClienteNoValidoException;
import pe.gob.pj.prueba.domain.exceptions.general.DominiosClientesNoPermitidosException;
import pe.gob.pj.prueba.domain.exceptions.general.ParametrosConsumoNoDesencriptadosException;
import pe.gob.pj.prueba.domain.model.seguridad.RoleSecurity;
import pe.gob.pj.prueba.domain.model.seguridad.query.AutenticacionUsuarioQuery;
import pe.gob.pj.prueba.domain.port.usecase.seguridad.AutenticarClienteUseCasePort;
import pe.gob.pj.prueba.infraestructure.common.enums.HttpCabeceraInfo;
import pe.gob.pj.prueba.infraestructure.common.enums.JwtPropiedades;
import pe.gob.pj.prueba.infraestructure.common.utils.EncryptUtils;
import pe.gob.pj.prueba.infraestructure.common.utils.InfraestructureConstant;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;
import pe.gob.pj.prueba.infraestructure.security.adapters.CustomUserDetails;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtLoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  final AuthenticationManager authenticationManager;
  final AutenticarClienteUseCasePort autenticarClienteUseCasePort;
  AutenticacionUsuarioQuery query;
  final SeguridadProperty seguridadProperties;
  final EncryptUtils encryptUtils;

  public JwtLoginAuthenticationFilter(AuthenticationManager authenticationManager,
      AutenticarClienteUseCasePort autenticarClienteUseCasePort,
      SeguridadProperty seguridadProperties, EncryptUtils encryptUtils) {
    this.authenticationManager = authenticationManager;
    this.autenticarClienteUseCasePort = autenticarClienteUseCasePort;
    this.seguridadProperties = seguridadProperties;
    this.encryptUtils = encryptUtils;
    setFilterProcessesUrl(JwtPropiedades.URL_AUTHENTICATE.getValor());
  }

  /**
   * Descripción : evalua la autenticacion del usuario
   * 
   * @param HttpServletRequest request - peticion HTTP
   * @param HttpServletResponse response - respuesta HTTP
   * @return Authentication - respuesta de la evaluacion de usuario
   * @exception Captura excepcion generica
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {

    var cuo = ProjectUtils.obtenerCodigoUnico();
    String referer = request.getHeader(HttpCabeceraInfo.REFERER.getNombre());

    if (!dominioPermitido(referer)) {
      log.error("{} Origen de petición no permitido {}, dominios permitidos {}", cuo, referer,
          Arrays.toString(seguridadProperties.dominios().getPermitidosArray()));
      throw new DominiosClientesNoPermitidosException(
          String.format("%s Origen de petición no permitido %s, dominios permitidos %s", cuo,
              referer, Arrays.toString(seguridadProperties.dominios().getPermitidosArray())));
    }

    var username = request.getHeader(HttpCabeceraInfo.USERNAME.getNombre());
    var password = request.getHeader(HttpCabeceraInfo.PASSWORD.getNombre());
    var codigoRol = request.getHeader(HttpCabeceraInfo.COD_ROL.getNombre());

    username = desencriptarParametroConsumo(cuo, username);
    password = desencriptarParametroConsumo(cuo, password);
    codigoRol = desencriptarParametroConsumo(cuo, codigoRol);

    query = AutenticacionUsuarioQuery.builder().usuario(username).clave(password)
        .codigoRol(codigoRol).build();
    var idUsuario = autenticarClienteUseCasePort.autenticarUsuario(cuo, query);

    if (Objects.isNull(idUsuario) || idUsuario.isEmpty()) {
      log.error(
          "{} Ocurrió un error en la autenticación de los parámetros de consumo del servicio.",
          cuo);
      throw new ClienteNoValidoException(String.format(
          "%s Ocurrió un error en la autenticación de los parámetros de consumo del servicio.",
          cuo));
    }

    return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(idUsuario,
        encryptUtils.encrypt(username, password)));
  }
  
  private String desencriptarParametroConsumo(String cuo, String textoEncriptado) {
    try {
      return encryptUtils.decryptPastFrass(textoEncriptado);
    } catch (Exception e) {
      log.error(
          "{} Ocurrió un error en la desencriptación del parámetro de consumo del servicio [{}], error [{}].",
          cuo, textoEncriptado, e);
      log.error("{} Key para desencriptar [{}]", cuo, seguridadProperties.key());
      throw new ParametrosConsumoNoDesencriptadosException(String.format(
          "%s Ocurrió un error en la desencriptación del parámetro de consumo del servicio [%s], error [%s].",
          cuo, textoEncriptado, e));
    }
  }

  private boolean dominioPermitido(String referer) {
    return Arrays.asList(seguridadProperties.dominios().getPermitidosArray()).stream()
        .anyMatch(InfraestructureConstant.TODO_DOMINIO_PERMITIDO::contains)
        || (Objects.nonNull(referer)
            && Arrays.asList(seguridadProperties.dominios().getPermitidosArray()).stream()
                .anyMatch(referer::contains));
  }

  /**
   * Descripción : Procesa la evaluacion positiva y genera el token
   * 
   * @param HttpServletRequest request - peticion HTTP
   * @param HttpServletResponse response - respuesta HTTP
   * @param FilterChain filterChain - cadenas filtro
   * @param Authentication authentication - resultado de la evaluacion
   * @return void
   * @exception Captura excepcion generica
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain, Authentication authentication) throws IOException {

    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    List<String> roles = user.getRoles().stream().map(RoleSecurity::getCodigo).toList();

    byte[] signingKey = JwtPropiedades.FIRMA.getValor().getBytes();

    Date ahora = new Date();
    var tiempoSegundosExpira =
        seguridadProperties.getTokenExpira();
    var tiempoSegundosRefresh =
        seguridadProperties.getTokenRefresh();
    String codigoRolSeleccionado = query.codigoRol();

    String token = Jwts.builder().signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
        .setHeaderParam(JwtPropiedades.CLAIM_TIPO.getNombre(), JwtPropiedades.CLAIM_TIPO.getValor())
        .setIssuer(JwtPropiedades.CLAIM_EMISOR.getValor())
        .setAudience(JwtPropiedades.CLAIM_DESTINO.getValor()).setSubject(user.getUsername())
        .setExpiration(ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira))
        .claim(JwtPropiedades.CLAIM_ROLES.getNombre(), roles)
        .claim(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre(), codigoRolSeleccionado)
        .claim(JwtPropiedades.CLAIM_USUARIO_AUTHENTICATE.getNombre(), user.getUsername())
        .claim(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre(), obtenerIp(request))
        .claim(JwtPropiedades.CLAIM_LIMITE_TOKEN.getNombre(),
            ProjectUtils.sumarRestarSegundos(ahora, tiempoSegundosExpira + tiempoSegundosRefresh))
        .compact();
    response.addHeader(HttpCabeceraInfo.AUTHORIZATION.getNombre(),
        JwtPropiedades.PREFIJO.getValor() + token);
    response.setContentType("application/json");
    response.getWriter().write("{\"token\":\"" + token + "\",\"exps\":\"" + tiempoSegundosExpira
        + "\",\"refs\":\"" + tiempoSegundosRefresh + "\"}");
  }

  private String obtenerIp(HttpServletRequest request) {
    if (!ProjectUtils.isNullOrEmpty(request.getRemoteAddr())) {
      return request.getRemoteAddr();
    }
    return request.getRemoteHost();
  }


  /**
   * Descripción : Procesa la evaluacion negativa
   * 
   * @param HttpServletRequest request - peticion HTTP
   * @param HttpServletResponse response - respuesta HTTP
   * @param AuthenticationException failed - excepcion por el fallo
   * @return void
   * @exception Captura excepcion generica
   */
  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException failed) {
    log.error("ERROR CON LA UTORIZACION DE SPRING SECURITY: " + failed.getMessage());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
