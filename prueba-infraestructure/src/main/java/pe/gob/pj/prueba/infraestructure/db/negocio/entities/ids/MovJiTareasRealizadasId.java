package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MovJiTareasRealizadasId implements Serializable {
    private Long justiciaItineranteId;
    private Long tareaId;
}