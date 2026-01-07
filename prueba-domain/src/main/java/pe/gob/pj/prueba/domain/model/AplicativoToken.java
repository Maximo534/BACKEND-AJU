package pe.gob.pj.prueba.domain.model;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AplicativoToken implements Serializable {

  private static final long serialVersionUID = 1L;

  String token;

}
