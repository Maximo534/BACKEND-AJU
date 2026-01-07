package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpcionResponse {

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
