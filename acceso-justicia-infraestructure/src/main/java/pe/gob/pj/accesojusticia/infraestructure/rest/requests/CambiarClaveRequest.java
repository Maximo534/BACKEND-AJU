package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
public class CambiarClaveRequest implements Serializable {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String claveActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nuevaClave;

    @NotBlank(message = "Debe confirmar la nueva contraseña")
    private String confirmarClave;
}