package pe.gob.pj.accesojusticia.infraestructure.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@RequiredArgsConstructor
public enum AplicativoInfo {
  
  NOMBRE_COMPONENTE("nombre","acceso-justicia-api-rest"),
  TIPO_COMPONENTE("tipo","api-rest"),
  CONEXTO("contexto","acceso-justicia-api"),
  VERSION_ACTUAL("version","1.0.0")
  ;
  
  String propiedad;
  String nombre;
  
}
