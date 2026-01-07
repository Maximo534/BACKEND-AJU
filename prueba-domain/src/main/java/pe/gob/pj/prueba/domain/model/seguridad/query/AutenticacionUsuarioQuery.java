package pe.gob.pj.prueba.domain.model.seguridad.query;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Modelo que envuelve los criterios de validacion para consumo de endpoints
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Builder
@Accessors(fluent = true)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutenticacionUsuarioQuery {

  String usuario;
  String clave;
  String codigoRol;
  String codigoCliente;

}
