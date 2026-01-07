package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;
import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovJiPersonasBeneficiadasId implements Serializable {
    private String justiciaItineranteId;
    private String descripcionRango;
    private String codigoRango;
}