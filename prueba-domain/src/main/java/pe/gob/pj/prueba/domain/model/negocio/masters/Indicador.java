package pe.gob.pj.prueba.domain.model.negocio.masters;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Indicador {
    private String id;
    private String descripcion;
    private String actividadId;
}