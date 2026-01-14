package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarBuenaPracticaRequest implements Serializable {

    // ✅ BUSCADOR GENERAL (Reemplaza a palabraClave)
    private String search;

    // FILTROS ESPECÍFICOS (Combos)
    private String distritoJudicialId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}