package pe.gob.pj.prueba.domain.model.negocio.query;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Encapsula credenciales para validacion de sesión
 * 
 * @author oruizb
 * @version 1.0
 */
@Accessors(fluent = true)
@Builder
@Setter @Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IniciarSesionQuery {
  
  String usuario;
  String clave;
  String aplicaCaptcha;
  String tokenCaptcha;

}
