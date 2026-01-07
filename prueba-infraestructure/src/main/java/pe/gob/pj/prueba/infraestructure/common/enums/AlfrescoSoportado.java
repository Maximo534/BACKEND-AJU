package pe.gob.pj.prueba.infraestructure.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Getter
@RequiredArgsConstructor
public enum AlfrescoSoportado {
  
  VERSION_4_1("4.1"), VERSION_4_2("4.2");
  
  String version;

}
