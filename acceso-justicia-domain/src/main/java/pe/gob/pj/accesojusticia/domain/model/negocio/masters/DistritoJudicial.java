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
public class DistritoJudicial implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String nombre;
    String nombreCorto;
    String sigla;
    String activo;
}