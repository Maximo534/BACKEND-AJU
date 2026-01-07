package pe.gob.pj.prueba.domain.model.negocio.query;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Encapsula los criterios de busqueda de persona
 * 
 * @author oruizb
 * @version 1.0
 */
@Accessors(fluent = true)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ConsultarPersonaQuery {

  String documentoIdentidad;
  
}
