package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovEventoTareaId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventoId;
    private String tareaId;

}