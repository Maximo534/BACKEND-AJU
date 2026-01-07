package pe.gob.pj.prueba.domain.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Envuelve los formatos manejados
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum Formatos {
  
  FECHA_YYYYMMDD ("yyyyMMdd"),
  FECHA_YYYY_MM_SLASH ("yyyy/MM/dd"),
  FECHA_YYYY_MM_DD_GUION ("yyyy-MM-dd"),
  FECHA_DD_MM_YYYY ("dd/MM/yyyy"),
  FECHA_DD_MM_YYYY_HH_MM ("dd/MM/yyyy hh:mm a"),
  FECHA_DD_MM_YYYY_HH_MM_SS ("dd/MM/yyyy hh:mm:ss"),
  FECHA_DD_MM_YYYY_HH_MM_SS_SSS ("dd/MM/yyyy HH:mm:ss.SSS"),
  FECHA_YYYY_MM_DD_HH_MM_SS_SSS ("yyyy-MM-dd HH:mm:ss.SSS")
  ;
  
  String formato;
  
}
