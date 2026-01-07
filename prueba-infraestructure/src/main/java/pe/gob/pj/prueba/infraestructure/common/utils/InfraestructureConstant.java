package pe.gob.pj.prueba.infraestructure.common.utils;

import java.util.List;
import lombok.experimental.UtilityClass;
import pe.gob.pj.prueba.infraestructure.common.enums.HttpCabeceraInfo;

@UtilityClass
public class InfraestructureConstant {

  public static final String TODO_DOMINIO_PERMITIDO = "*";
  public static final String MENSAJE_ERROR_USUARIO =
      "Ocurrió un error, comuniquese con el área de soporte.";

  public static final List<String> ALLOWED_HEADERS =
      List.of(HttpCabeceraInfo.AUTHORIZATION.getNombre(), "Content-Type", "X-Requested-With",
          "Accept", "Accept-Language", "Origin", "Cache-Control", "Pragma",
          HttpCabeceraInfo.USER_AGENT.getNombre(), HttpCabeceraInfo.USERNAME.getNombre(),
          HttpCabeceraInfo.PASSWORD.getNombre(), HttpCabeceraInfo.COD_ROL.getNombre(),
          HttpCabeceraInfo.AUD_USUARIO_APLICATIVO.getNombre(),
          HttpCabeceraInfo.AUD_USUARIO_RED.getNombre(), HttpCabeceraInfo.AUD_PC.getNombre(),
          HttpCabeceraInfo.AUD_MAC.getNombre(), HttpCabeceraInfo.AUD_IP.getNombre());

  public static final List<String> WHITELIST_PATHS = List.of("/healthcheck", "/swagger-ui/**",
      "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui.html");

}
