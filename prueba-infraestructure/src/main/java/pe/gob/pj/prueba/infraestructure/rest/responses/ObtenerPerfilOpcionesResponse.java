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
public class ObtenerPerfilOpcionesResponse extends GlobalResponse implements Serializable {

  static final long serialVersionUID = 1L;
  
  OpcionesPerfilResponse data;
  
  public ObtenerPerfilOpcionesResponse(String cuo, OpcionesPerfilResponse data) {
    super(cuo);
    this.data = data;
  }

}
