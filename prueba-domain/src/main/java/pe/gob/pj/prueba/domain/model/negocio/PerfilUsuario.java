package pe.gob.pj.prueba.domain.model.negocio;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author oruizb
 * @version 1.0
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PerfilUsuario implements Serializable {

  static final long serialVersionUID = 1L;

  Integer id;
  Integer idPerfil;
  String nombre;
  String rol;

}
