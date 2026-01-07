package pe.gob.pj.prueba.infraestructure.security.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.enums.Flag;
import pe.gob.pj.prueba.domain.common.utils.ProjectConstants;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.exceptions.general.IpTokenDifiereIpEndpointException;
import pe.gob.pj.prueba.domain.exceptions.general.TokenExpiradoException;
import pe.gob.pj.prueba.domain.exceptions.general.TokenNoValidoException;
import pe.gob.pj.prueba.domain.exceptions.general.UsuarioRolNoTienePermisosException;
import pe.gob.pj.prueba.domain.exceptions.general.UsuarioTokenNoValidoException;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.seguridad.query.AutorizacionPeticionQuery;
import pe.gob.pj.prueba.domain.port.usecase.seguridad.ValidarAutorizacionUseCasePort;
import pe.gob.pj.prueba.infraestructure.common.enums.HttpCabeceraInfo;
import pe.gob.pj.prueba.infraestructure.common.enums.JwtPropiedades;
import pe.gob.pj.prueba.infraestructure.common.enums.TipoError;
import pe.gob.pj.prueba.infraestructure.common.utils.InfraestructureConstant;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;
import pe.gob.pj.prueba.infraestructure.rest.responses.ErrorResponse;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

  static final String URI_REFRESH_TOKEN = "seguridad/refresh";
  ValidarAutorizacionUseCasePort validarAutorizacionUseCasePort;
  SeguridadProperty seguridadProperties;

  public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
      ValidarAutorizacionUseCasePort validarAutorizacionUseCasePort,
      SeguridadProperty seguridadProperties) {
    super(authenticationManager);
    this.validarAutorizacionUseCasePort = validarAutorizacionUseCasePort;
    this.seguridadProperties = seguridadProperties;
  }


  /**
   * Descripción : filtra las peticiones HTTP y evalua el token
   * 
   * @param HttpServletRequest request - peticion HTTP
   * @param HttpServletResponse response, - respuesta HTTP
   * @param FilterChain filterChain - cadenas filtro
   * @return void
   * @exception Captura excepcion generica
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    agregarAtributos(request);

    if (!dominioPermitido(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (pathNoRequiereAutorizacion(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
      if (authentication != null) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (UsuarioTokenNoValidoException | TokenNoValidoException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      new ObjectMapper().writeValue(response.getWriter(),
          new ErrorResponse("No autenticado."));
      response.flushBuffer();
    } catch (UsuarioRolNoTienePermisosException | IpTokenDifiereIpEndpointException | TokenExpiradoException e) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      new ObjectMapper().writeValue(response.getWriter(),
          new ErrorResponse("No autorizado."));
      response.flushBuffer();
    }

    filterChain.doFilter(request, response);

  }

  private boolean pathNoRequiereAutorizacion(HttpServletRequest request) {
    var pathMatcher = new AntPathMatcher();
    var path = request.getRequestURI().substring(request.getContextPath().length());
    return InfraestructureConstant.WHITELIST_PATHS.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  private boolean dominioPermitido(HttpServletRequest request) {
    String referer = request.getHeader("Referer");
    String todos = "*";

    if (!todos
        .equalsIgnoreCase(Arrays.toString(seguridadProperties.dominios().getPermitidosArray()))) {
      log.error("Dominios permitidos: {} Origen: [{}]",
          Arrays.toString(seguridadProperties.dominios().getPermitidosArray()),
          Optional.ofNullable(referer).orElse("No se pudo identificar."));
    }

    return Arrays.asList(seguridadProperties.dominios().getPermitidosArray()).stream()
        .anyMatch(todos::contains)
        || (Objects.nonNull(referer)
            && Arrays.asList(seguridadProperties.dominios().getPermitidosArray()).stream()
                .anyMatch(referer::contains));
  }

  private void agregarAtributos(HttpServletRequest request) {
    request.setAttribute(ProjectConstants.PETICION, PeticionServicios.builder()
        .cuo(ProjectUtils.obtenerCodigoUnico()).tipoMetodoHttp(request.getMethod())
        .usuarioAuth(Flag.VACIO.getCodigo()).uri(request.getRequestURI())
        .params(Optional.ofNullable(request.getQueryString()).orElse(Flag.VACIO.getCodigo()))
        .herramienta(request.getHeader(HttpCabeceraInfo.USER_AGENT.getNombre()))
        .ipPublica(obtenerIp(request)).ips(obtenerIps(request))
        .usuario(request.getHeader(HttpCabeceraInfo.AUD_USUARIO_APLICATIVO.getNombre()))
        .red(request.getHeader(HttpCabeceraInfo.AUD_USUARIO_RED.getNombre()))
        .ip(request.getHeader(HttpCabeceraInfo.AUD_IP.getNombre()))
        .nombrePc(request.getHeader(HttpCabeceraInfo.AUD_PC.getNombre()))
        .codigoMac(request.getHeader(HttpCabeceraInfo.AUD_MAC.getNombre()))
        .inicio(System.currentTimeMillis()).codigoRespuesta(TipoError.OPERACION_EXITOSA.getCodigo())
        .descripcionRespuesta(TipoError.OPERACION_EXITOSA.getDescripcionUsuario()).build());
  }

  private String obtenerIp(HttpServletRequest request) {
    if (!ProjectUtils.isNullOrEmpty(request.getRemoteAddr())) {
      return request.getRemoteAddr();
    }
    return request.getRemoteHost();
  }

  private String obtenerIps(HttpServletRequest request) {
    var mainHeaders =
        Stream.of("X-Forwarded-For", "X-Real-IP").map(request::getHeader).filter(Objects::nonNull)
            .flatMap(header -> Arrays.stream(header.split(",")).map(String::trim));

    var legacyHeaders =
        Stream.of("Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR")
            .map(request::getHeader).filter(Objects::nonNull);

    return Stream
        .concat(Stream.of(request.getRemoteAddr(), request.getRemoteHost()),
            Stream.concat(mainHeaders, legacyHeaders))
        .filter(ip -> !ip.isEmpty()).distinct().collect(Collectors.joining("|"));
  }

  /**
   * Descripción : obtiene la autenticacion desde token
   * 
   * @param HttpServletRequest request - peticion HTTP
   * @return UsernamePasswordAuthenticationToken - Informacion de autenticacion proveniente token
   * @exception Captura excepcion generica
   */
  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

    var peticion = (PeticionServicios) request.getAttribute(ProjectConstants.PETICION);
    var token = request.getHeader(HttpCabeceraInfo.AUTHORIZATION.getNombre());

    validarFormaToken(peticion, token);

    try {
      var jwt = token.replace(JwtPropiedades.PREFIJO.getValor(), "");
      Jws<Claims> parsedToken = parsearToken(jwt, JwtPropiedades.FIRMA.getValor().getBytes());

      validarIpOrigen(peticion, parsedToken.getBody()
          .get(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre()).toString());

      actualizarDatosPeticion(request, peticion, parsedToken);
      validarUsuarioToken(peticion, parsedToken);
      validarAutorizacionJwt(peticion, parsedToken);

      List<SimpleGrantedAuthority> authorities =
          ((List<?>) parsedToken.getBody().get(JwtPropiedades.CLAIM_ROLES.getNombre())).stream()
              .map(authority -> new SimpleGrantedAuthority((String) authority)).toList();

      return new UsernamePasswordAuthenticationToken(parsedToken.getBody().getSubject(), null,
          authorities);

    } catch (ExpiredJwtException exception) {
      log.error("{} Token enviado ha expirado : {}", peticion.getCuo(), exception.getMessage());
      validarIpOrigen(peticion, exception.getClaims()
          .get(JwtPropiedades.CLAIM_IP_REALIZA_PETICION.getNombre()).toString());
      return handleExpiredJwt(peticion, exception.getClaims());
    }

  }

  private void actualizarDatosPeticion(HttpServletRequest request, PeticionServicios peticion,
      Jws<Claims> parsedToken) {
    peticion.setUsuarioAuth(parsedToken.getBody().getSubject());
    peticion
        .setJwt(Optional.ofNullable(request.getHeader(HttpCabeceraInfo.AUTHORIZATION.getNombre()))
            .map(token -> token.replace(JwtPropiedades.PREFIJO.getValor(), "")).orElse(null));
    peticion.setUsuario(Optional
        .ofNullable(parsedToken.getBody().get(JwtPropiedades.CLAIM_USUARIO_APLICATIVO.getNombre()))
        .map(Object::toString)
        .orElse(request.getHeader(HttpCabeceraInfo.AUD_USUARIO_APLICATIVO.getNombre())));
  }


  private void validarUsuarioToken(PeticionServicios peticion, Jws<Claims> parsedToken) {
    var username = parsedToken.getBody().getSubject();
    if (ProjectUtils.isNullOrEmpty(username)) {
      log.error("{} El usuario [{}] del token, tiene valor no válido.", peticion.getCuo(),
          username);
      throw new UsuarioTokenNoValidoException(String.format(
          "%s El usuario [%s] del token, tiene valor no válido.", peticion.getCuo(), username));
    }
  }

  private void validarAutorizacionJwt(PeticionServicios peticion, Jws<Claims> parsedToken) {

    var username = parsedToken.getBody().getSubject();
    var rolSeleccionado = Optional
        .ofNullable(parsedToken.getBody().get(JwtPropiedades.CLAIM_ROL_APLICATIVO.getNombre()))
        .map(Object::toString).orElse(parsedToken.getBody()
            .get(JwtPropiedades.CLAIM_ROL_AUTHENTICATE.getNombre()).toString());
    if (!peticion.getUri().endsWith(URI_REFRESH_TOKEN) && validarAutorizacionUseCasePort
        .validarAccesoMetodo(peticion.getCuo(),
            AutorizacionPeticionQuery.builder().usuario(username).rol(rolSeleccionado)
                .operacion(peticion.getUri()).tipoOperacion(peticion.getTipoMetodoHttp()).build())
        .isEmpty()) {
      log.error("{} El usuario [{}] con rol [{}], no tiene acceso al método [{}][{}]",
          peticion.getCuo(), username, rolSeleccionado, peticion.getTipoMetodoHttp(),
          peticion.getUri());
      throw new UsuarioRolNoTienePermisosException(String.format(
          "%s El usuario [%s] con rol [%s], no tiene acceso al método [%s][%s]", peticion.getCuo(),
          username, rolSeleccionado, peticion.getTipoMetodoHttp(), peticion.getUri()));
    }

  }


  private void validarFormaToken(PeticionServicios peticion, String token) {
    if (ProjectUtils.isNullOrEmpty(token) || !token.startsWith(JwtPropiedades.PREFIJO.getValor())) {
      log.error("{} El token enviado es null, vacío o no tiene formato válido.", peticion.getCuo());
      throw new TokenNoValidoException("");
    }
  }

  private void validarIpOrigen(PeticionServicios peticion, String ipToken) {
    if (!peticion.getIpPublica().equals(ipToken)) {
      log.error(
          "{} La ip que generó el token {} no coincide con la ip desde donde se consume el método {}",
          peticion.getCuo(), ipToken, peticion.getIpPublica());
      throw new IpTokenDifiereIpEndpointException(String.format(
          "%s La ip que generó el token %s no coincide con la ip desde donde se consume el método %s",
          peticion.getCuo(), ipToken, peticion.getIpPublica()));
    }
  }

  private UsernamePasswordAuthenticationToken handleExpiredJwt(PeticionServicios peticion,
      Claims claims) {

    if (!peticion.getUri().endsWith(URI_REFRESH_TOKEN)) {
      log.error(
          "{} El token de la petición ha expirado, el endpoint no permite renovación o la IP del token no coincide con la actual.",
          peticion.getCuo());
      throw new TokenExpiradoException();
    }

    String subject = claims.getSubject();
    List<SimpleGrantedAuthority> authorities =
        ((List<?>) claims.get(JwtPropiedades.CLAIM_ROLES.getNombre())).stream()
            .map(authority -> new SimpleGrantedAuthority((String) authority)).toList();
    return new UsernamePasswordAuthenticationToken(subject, null, authorities);

  }

  private Jws<Claims> parsearToken(String token, byte[] signingKey) {
    return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(signingKey)).build()
        .parseClaimsJws(token);
  }

}
