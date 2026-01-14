package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarFfcRequest implements Serializable {

    // ✅ BUSCADOR GENERAL (Input texto)
    private String search;

    // ✅ FILTROS ESPECÍFICOS (Combos)
    private String distritoJudicialId;
    private String tipoEvento; // Ahora es un combo (ID o Código del tipo)

    // FECHAS
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}