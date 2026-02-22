package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarUsuarioRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Integer idUsuario;
    String usuario;
    String nombreCompleto;
    String activo;

    String formatoRespuesta;
}