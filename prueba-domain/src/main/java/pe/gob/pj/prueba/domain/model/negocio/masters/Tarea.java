package pe.gob.pj.prueba.domain.model.negocio.masters;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tarea {
    private String id;
    private String descripcion;
    private String medida;
    private String tipoDato;
    private String indicadorId;
}
