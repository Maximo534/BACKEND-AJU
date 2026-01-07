package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovJiCasosAtendidosId implements Serializable {
    private String justiciaItineranteId;
    private Integer materiaId;
}