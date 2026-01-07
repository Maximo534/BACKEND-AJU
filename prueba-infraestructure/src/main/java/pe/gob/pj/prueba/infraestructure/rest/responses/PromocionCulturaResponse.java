package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class PromocionCulturaResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String nombreActividad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String lugarActividad;
    private String estado;
}