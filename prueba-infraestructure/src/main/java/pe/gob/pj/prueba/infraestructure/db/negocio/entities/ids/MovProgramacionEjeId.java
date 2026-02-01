package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovProgramacionEjeId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idDistritoJudicial;
    private String periodo;
    private Integer idEje;
    private Integer idUsuario;

}