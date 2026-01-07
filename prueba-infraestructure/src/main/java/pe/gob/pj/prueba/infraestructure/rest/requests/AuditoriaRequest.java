package pe.gob.pj.prueba.infraestructure.rest.requests;

import org.hibernate.validator.constraints.Length;
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
public class AuditoriaRequest {

  @NotBlank(message = "El xAudNombreUsuario tiene un valor incorrecto")
  @NotNull(message = "El xAudNombreUsuario no puede ser null.")
  @Length(max = 20,
      message = "El nombre de usuario de red  de solicitante tiene una longitud que ha superado el máximo permitido.")
  @JsonProperty("usuario")
  String usuario;

  @NotBlank(message = "El xAudNombrePC tiene un valor incorrecto.")
  @NotNull(message = "El xAudNombrePC no puede ser null.")
  @Length(max = 20,
      message = "El nombre de PC tiene una longitud que ha superado el máximo permitido.")
  @JsonProperty("nombrePc")
  String nombrePc;

  @NotBlank(message = "El xAudIP tiene un valor incorrecto")
  @NotNull(message = "El xAudIP no puede ser null")
  @Pattern(regexp = PatronValidacionConstants.IP,
      message = "La IP de solicitante no tiene un formato válido.")
  @Length(max = 15,
      message = "La IP de solicitante tiene una longitud que ha superado el máximo permitido.")
  @JsonProperty("numeroIp")
  String numeroIp;

  @NotBlank(message = "El xAudDireccionMAC tiene un valor incorrecto.")
  @NotNull(message = "El xAudDireccionMAC no puede ser null.")
  @Pattern(regexp = PatronValidacionConstants.MAC,
      message = "La dirección MAC address es incorrecta.")
  @Length(min = 17, max = 17,
      message = "Dirección MAC address de la PC no tiene una longitud permitida.")
  @JsonProperty("direccionMac")
  String direccionMac;

}
