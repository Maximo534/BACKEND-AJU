package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovLljPersonasAtendidasId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String lljId;
    private Integer tipoVulnerabilidadId;
    private String rangoEdad;
}