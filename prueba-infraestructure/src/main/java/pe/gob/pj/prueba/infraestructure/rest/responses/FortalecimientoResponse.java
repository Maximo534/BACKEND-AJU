package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class FortalecimientoResponse implements Serializable {
    private String id;
    private String nombreEvento;
    private String tipoEvento;
    private LocalDate fechaInicio;
    private String lugar;
    private LocalDate fechaRegistro;
    private String estado;
}