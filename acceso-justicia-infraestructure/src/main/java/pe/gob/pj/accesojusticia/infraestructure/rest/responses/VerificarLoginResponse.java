package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificarLoginResponse implements Serializable {

    static final long serialVersionUID = 1L;

    String login;
    boolean disponible;
    String mensaje;
}