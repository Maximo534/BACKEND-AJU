package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class JuezPazEscolarResponse implements Serializable {
    private String id;
    private String nombreCompleto; // Concatenado
    private String cargo;
    private String nombreColegio;
}