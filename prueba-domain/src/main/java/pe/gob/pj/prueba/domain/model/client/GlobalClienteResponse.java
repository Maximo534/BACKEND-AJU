package pe.gob.pj.prueba.domain.model.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public abstract class GlobalClienteResponse {
  
  String codigo;
  String descripcion;
  String codigoOperacion;
  
}
