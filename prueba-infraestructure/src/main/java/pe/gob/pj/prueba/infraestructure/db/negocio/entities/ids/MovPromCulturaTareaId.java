package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovPromCulturaTareaId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String promocionCulturaId;
    private String tareaId;
}