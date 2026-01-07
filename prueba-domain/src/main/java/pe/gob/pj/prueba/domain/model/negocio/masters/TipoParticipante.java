package pe.gob.pj.prueba.domain.model.negocio.masters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoParticipante {
    private Integer id;
    private String descripcion;
    private String detalle;
}