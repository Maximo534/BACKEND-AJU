package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarItineranteRequest implements Serializable {
    private String search;

    private Long distritoJudicialId;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

}