package pe.gob.pj.prueba.infraestructure.rest.responses;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.AplicativoToken;

@EqualsAndHashCode(callSuper = true)
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefrescarTokenResponse extends GlobalResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  AplicativoToken data;

  public RefrescarTokenResponse(String cuo, AplicativoToken data) {
    super(cuo);
    this.data = data;
  }

}
