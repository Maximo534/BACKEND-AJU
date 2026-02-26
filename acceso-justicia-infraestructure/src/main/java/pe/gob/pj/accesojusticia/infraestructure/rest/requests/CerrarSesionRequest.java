package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CerrarSesionRequest implements Serializable {

    static final long serialVersionUID = 1L;

    @NotNull(message = "El id de sesión es requerido")
    Long idSesion;

}