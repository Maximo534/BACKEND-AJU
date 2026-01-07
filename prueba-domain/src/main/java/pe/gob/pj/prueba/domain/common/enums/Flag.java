package pe.gob.pj.prueba.domain.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Engloba caracteres manejados en la logica del proyecto
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum Flag {
  
  SI("S"), NO("N"), VACIO("");

  String codigo;

}
