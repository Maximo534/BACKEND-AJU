package pe.gob.pj.prueba.domain.model.negocio;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.Auditoria;

/**
 * @author oruizb
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Persona extends Auditoria implements Serializable {

  static final long serialVersionUID = 1L;

  public static final String P_NUMERO_DOCUMENTO = "numeroDocumentoPersona";

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
