package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarItineranteRequest implements Serializable {
    private String codigoRegistro;
    private String publicoObjetivo;
    private String distritoJudicialId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

}