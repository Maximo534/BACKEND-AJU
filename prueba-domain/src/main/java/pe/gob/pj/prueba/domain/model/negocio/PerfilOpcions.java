package pe.gob.pj.prueba.domain.model.negocio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author oruizb
 * @version 1.0
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PerfilOpcions implements Serializable {

  static final long serialVersionUID = 1L;

  String rol;
  List<Opcion> opciones = new ArrayList<>();

}
