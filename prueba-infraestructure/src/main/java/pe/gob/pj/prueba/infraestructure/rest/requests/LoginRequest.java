package pe.gob.pj.prueba.infraestructure.rest.requests;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.utils.PatronValidacionConstants;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest implements Serializable {

  /**
   * 
   */
  static final long serialVersionUID = 1L;

  @JsonProperty(value = "formatoRespuesta")
  String formatoRespuesta;

  @NotBlank(message = "El parámetro usuario es obligatorio, no puede ser nulo o vacío.")
  @JsonProperty(value = "usuario")
  String usuario;

  @NotBlank(message = "El parámetro clave es obligatorio, no puede ser nulo o vacío.")
  @JsonProperty(value = "clave")
  String clave;

  @Pattern(regexp = PatronValidacionConstants.S_N,
      message = "El parámetro aplicaCaptcha tiene un formato no válido [S|N].")
  @NotNull(message = "El parámetro aplicaCaptcha no puede ser nulo.")
  @JsonProperty("aplicaCaptcha")
  String aplicaCaptcha;

  @JsonProperty("tokenCaptcha")
  String tokenCaptcha;

}
