package pe.gob.pj.prueba.infraestructure.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FormatoRespuestaPeticion {

  XML("XML", "Formato de respuesta xml"), JSON("JSON", "Formato de respuesta json");

  private final String nombre;
  private final String descripcion;

}
