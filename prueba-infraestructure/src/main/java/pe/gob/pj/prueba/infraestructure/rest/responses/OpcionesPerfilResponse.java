package pe.gob.pj.prueba.infraestructure.rest.responses;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpcionesPerfilResponse {
  
  String rol;
  List<OpcionResponse> opciones = new ArrayList<>();
  String token;
  
}
