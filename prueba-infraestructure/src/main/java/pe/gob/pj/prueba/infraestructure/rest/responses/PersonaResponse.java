package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonaResponse {

  Integer id;
  String numeroDocumento;
  String fechaNacimiento;
  String primerApellido;
  String segundoApellido;
  String nombres;
  String sexo;
  String correo;
  String telefono;
  String activo;

  String idTipoDocumento;
  String tipoDocumento;

}
