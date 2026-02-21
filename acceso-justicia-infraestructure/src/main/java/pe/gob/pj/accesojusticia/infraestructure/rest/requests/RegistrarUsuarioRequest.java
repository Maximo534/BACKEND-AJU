package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import java.io.Serializable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarUsuarioRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Integer id;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 25, message = "El usuario no puede exceder 25 caracteres")
    @NotNull(message = "El usuario no puede ser nulo.")
    @Size(min = 8, message = "El usuario tiene una longitud no válida [min=8].")
    String usuario;

    String clave;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombreCompleto;

    @NotBlank(message = "El cargo es obligatorio")
    String cargo;

    String sigla;

    @Email(message = "El formato del correo no es válido")
    String email;

    @NotNull(message = "El distrito judicial es obligatorio")
    Integer idDistritoJudicial;

    Integer idInstancia;

    String rutaFoto;

    @NotNull(message = "El perfil es obligatorio")
    Integer idPerfil;

    Integer idEje;

    String formatoRespuesta;
}