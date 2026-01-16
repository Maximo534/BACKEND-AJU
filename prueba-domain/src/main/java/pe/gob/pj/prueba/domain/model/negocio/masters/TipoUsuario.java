package pe.gob.pj.prueba.domain.model.negocio.masters;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TipoUsuario implements Serializable {

    static final long serialVersionUID = 1L;

    String id;          // c_tipo_usu_id
    String descripcion; // x_descripcion
    String detalle;     // x_detalle
    String alcance;     // b_alcance
    String activo;      // l_activo

}