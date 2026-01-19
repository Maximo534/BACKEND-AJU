package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarBuenaPracticaRequest implements Serializable {

    private String search;

    private String distritoJudicialId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}