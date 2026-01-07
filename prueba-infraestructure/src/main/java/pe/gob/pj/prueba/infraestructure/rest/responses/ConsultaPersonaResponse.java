package pe.gob.pj.prueba.infraestructure.rest.responses;

import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author oruizb
 * @version 1.0, 01//01/2025
 */
@EqualsAndHashCode(callSuper = true)
@Setter @Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultaPersonaResponse extends GlobalResponse implements Serializable{
  
  private static final long serialVersionUID = 1L;
  
  List<PersonaResponse> data;
  
  public ConsultaPersonaResponse(String cuo, List<PersonaResponse> data) {
    super(cuo);
    this.data = data;
  }

}
