package pe.gob.pj.prueba.domain.model.negocio.masters;

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
public class Indicador implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String descripcion;
    Long actividadId;
    String activo;
}