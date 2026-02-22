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
public class MovJiPersonasAtendidasId implements Serializable {
    private Long justiciaItineranteId;
    private Long tipoVulnerabilidadId;
    private String rangoEdad;
}