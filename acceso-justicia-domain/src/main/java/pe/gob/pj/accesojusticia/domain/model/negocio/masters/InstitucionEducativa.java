package pe.gob.pj.accesojusticia.domain.model.negocio.masters;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstitucionEducativa implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;
    String nombre;
    String nivel;
    String director;
    String telefono;
    String direccion;
    Long ugelId;
    String activo;
}