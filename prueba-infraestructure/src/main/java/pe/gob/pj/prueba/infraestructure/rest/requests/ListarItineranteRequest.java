package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarItineranteRequest implements Serializable {
    private String search;

    // ✅ FILTRO COMBO
    private String distritoJudicialId;

    // ✅ FILTRO RANGO DE FECHAS (ejecución de la actividad)
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

}