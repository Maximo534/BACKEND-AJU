package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovLljCasosAtendidosId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "c_llj_id")
    private String lljId;

    @Column(name = "n_materia_id")
    private Integer materiaId;
}