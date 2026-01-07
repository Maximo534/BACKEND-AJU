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
public class UsuarioResponse {

  String usuario;
  String clave;
  PersonaResponse persona = new PersonaResponse();
  List<PerfilUsuarioResponse> perfiles = new ArrayList<>();

  String token;
}
