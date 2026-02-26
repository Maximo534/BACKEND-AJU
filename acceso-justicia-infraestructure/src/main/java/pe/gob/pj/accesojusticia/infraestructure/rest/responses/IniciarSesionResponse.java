package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IniciarSesionResponse extends GlobalResponse implements Serializable {

  static final long serialVersionUID = 1L;
  Long idSesion;
  UsuarioResponse data;
  
  public IniciarSesionResponse(String cuo, Long idSesion, UsuarioResponse data) {
    super(cuo);
    this.idSesion = idSesion;
    this.data = data;
  }

}
