package pe.gob.pj.prueba.domain.model.negocio;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @author oruizb
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Opcion implements Serializable {
  /**
  * 
  */
  static final long serialVersionUID = 1L;

  Integer id;
  String codigo;
  String url;
  String nombre;
  String descripcion;
  Integer orden;
  String icono;
  Integer idOpcionSuperior;
  String nombreOpcionSuperior;

  String activo;
  String perfiles;

}
