package pe.gob.pj.prueba.infraestructure.rest.responses;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AplicativoResponse extends GlobalResponse implements Serializable {

  private static final long serialVersionUID = 1L;
  Object data;
  
  public AplicativoResponse(String cuo, Object data) {
    super(cuo);
    this.data = data;
  }
  
}
