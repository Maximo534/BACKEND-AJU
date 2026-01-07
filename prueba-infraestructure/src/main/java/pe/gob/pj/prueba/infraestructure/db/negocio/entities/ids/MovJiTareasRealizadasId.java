package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;
import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovJiTareasRealizadasId implements Serializable {
    private String justiciaItineranteId;
    private String tareaId; //Cambiado a String (CHAR 15)
}