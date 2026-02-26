package pe.gob.pj.accesojusticia.domain.model.negocio;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sesion implements Serializable {

    static final long serialVersionUID = 1L;

    Long idSesion;
    Integer idUsuario;
    LocalDateTime fechaIngreso;
    LocalDateTime fechaSalida;
}