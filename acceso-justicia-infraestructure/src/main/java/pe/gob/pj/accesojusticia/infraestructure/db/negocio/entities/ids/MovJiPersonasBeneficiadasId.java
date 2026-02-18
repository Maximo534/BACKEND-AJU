package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.ids;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MovJiPersonasBeneficiadasId implements Serializable {
    private Long justiciaItineranteId;
    private String descripcionRango;
    private String codigoRango;
}