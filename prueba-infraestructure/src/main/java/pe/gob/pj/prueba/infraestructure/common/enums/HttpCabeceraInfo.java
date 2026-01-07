package pe.gob.pj.prueba.infraestructure.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum HttpCabeceraInfo {
  
  USER_AGENT("User-Agent"),
  AUTHORIZATION("Authorization"),
  USERNAME("username"),
  PASSWORD("password"),
  COD_ROL("codigoRol"),

  REFERER("Referer"),
  AUD_USUARIO_APLICATIVO("X-Request-Usuario-Aplicativo"),
  AUD_USUARIO_RED("X-Request-Usuario-Red"),
  AUD_PC("X-Request-Pc"),
  AUD_MAC("X-Request-Mac"),
  AUD_IP("X-Request-Ip")
  ;
  
  String nombre;
  
}
