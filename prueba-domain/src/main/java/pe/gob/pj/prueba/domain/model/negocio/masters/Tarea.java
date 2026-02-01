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
public class Tarea implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String descripcion;
    String medida;
    String tipoDato;
    Long indicadorId;
    String activo;
}