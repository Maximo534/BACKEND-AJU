package pe.gob.pj.prueba.infraestructure.rest.responses;

import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Formatos;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponse {

  private String error;
  private String timestamp;

  public ErrorResponse(String error) {
    this.error = error;
    this.timestamp = ProjectUtils.convertDateToString(new Date(),
        Formatos.FECHA_DD_MM_YYYY_HH_MM_SS_SSS.getFormato());
  }

}
