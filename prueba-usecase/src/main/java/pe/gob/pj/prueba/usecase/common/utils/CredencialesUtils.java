package pe.gob.pj.prueba.usecase.common.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitarios de encriptacion y validacion de credenciales de usuario
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Slf4j
@UtilityClass
public class CredencialesUtils {

  /**
   * Hashea la clave ingresada usando SpringSecurity
   * 
   * @param clave String cadena de caracteres indicados por el usuario
   * @return Clave hasheada
   */
  public String hashearClave(String clave) {
    return BCrypt.hashpw(clave, BCrypt.gensalt(16));
  }

  /**
   * Valida la clave ingresada por el usuario con la clave almacenada
   * 
   * @param cuo String código único de operación
   * @param clave String clave ingresada por el usuario
   * @param claveAlmacenado String clave almacenada en repositorio con el cual comparar
   * @return resultado de la validacion, true si coincidio y false si no lo hizo
   */
  public boolean validarClave(String cuo, String clave, String claveAlmacenado) {
    try {
      return BCrypt.checkpw(clave, claveAlmacenado);
    } catch (Exception e) {
      log.error("{} Error al validar clave: {}", cuo, e);
      return false;
    }
  }

}
