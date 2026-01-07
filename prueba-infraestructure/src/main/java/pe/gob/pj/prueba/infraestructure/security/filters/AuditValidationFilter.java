package pe.gob.pj.prueba.infraestructure.security.filters;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.domain.exceptions.general.AuditoriaNoEncontradaException;
import pe.gob.pj.prueba.infraestructure.common.enums.HttpCabeceraInfo;
import pe.gob.pj.prueba.infraestructure.common.utils.InfraestructureConstant;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;

@Slf4j
public class AuditValidationFilter extends OncePerRequestFilter {

  SeguridadProperty seguridadProperties;

  public AuditValidationFilter(SeguridadProperty seguridadProperties) {
    this.seguridadProperties = seguridadProperties;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    var auditoriaRequerida = seguridadProperties.auditoria().requerida();
    if (!pathNoRequiereAutorizacion(request)
        && !Estado.INACTIVO_LETRA.getNombre().equals(auditoriaRequerida)) {
      List.of(HttpCabeceraInfo.AUD_USUARIO_APLICATIVO, HttpCabeceraInfo.AUD_USUARIO_RED,
          HttpCabeceraInfo.AUD_PC, HttpCabeceraInfo.AUD_MAC, HttpCabeceraInfo.AUD_IP)
          .forEach(header -> Optional.ofNullable(request.getHeader(header.getNombre()))
              .orElseThrow(() -> {
                log.error(
                    "Error de auditoría: los datos del emisor en la cabecera de la petición son obligatorios (X-Request-Usuario-Aplicativo, X-Request-Usuario-Red, X-Request-Ip, X-Request-Pc y X-Request-Mac)");
                return new AuditoriaNoEncontradaException();
              }));
    }
    chain.doFilter(request, response);
  }

  private boolean pathNoRequiereAutorizacion(HttpServletRequest request) {
    var pathMatcher = new AntPathMatcher();
    var path = request.getRequestURI().substring(request.getContextPath().length());
    return InfraestructureConstant.WHITELIST_PATHS.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

}
