package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PerfilUsuarioResponse {
  Integer idPerfil;
  String nombre;
  String rol;
}
