package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarOrientadoraRequest implements Serializable {

    private String search; // Buscador por ID, Nombre o DNI

    // Filtros
    private String distritoJudicialId;

    // Rango de fechas (Atención)
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}