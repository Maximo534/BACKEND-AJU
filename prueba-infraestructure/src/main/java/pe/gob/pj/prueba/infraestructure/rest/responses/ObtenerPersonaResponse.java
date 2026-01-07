package pe.gob.pj.prueba.infraestructure.rest.responses;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.negocio.Persona;

@EqualsAndHashCode(callSuper = true)
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObtenerPersonaResponse extends GlobalResponse implements Serializable{

  static final long serialVersionUID = 1L;
  
  Persona data;
  
  public ObtenerPersonaResponse(String cuo, Persona data) {
    super(cuo);
    this.data = data;
  }

}
