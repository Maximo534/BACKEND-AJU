package pe.gob.pj.prueba.infraestructure.rest.requests;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ObtenerOpcionesRequest implements Serializable {
  /**
  * 
  */
  private static final long serialVersionUID = 1L;

  @JsonProperty(value = "formatoRespuesta")
  String formatoRespuesta;

  @NotNull(message = "El usuario no puede ser nulo.")
  @NotBlank(message = "El usuario no puede ser vacío.")
  @Size(min = 8, message = "El usuario tiene una longitud no válida [min=8].")
  @JsonProperty("usuario")
  private String usuario;

  @NotNull(message = "Se requiere enviar identificador de perfil.")
  @JsonProperty("idPerfil")
  private int idPerfil;

}
