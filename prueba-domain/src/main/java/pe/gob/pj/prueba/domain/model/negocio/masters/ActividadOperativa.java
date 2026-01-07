package pe.gob.pj.prueba.domain.model.negocio.masters;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActividadOperativa {
    private String id;
    private String descripcion;
    private String activo;
}