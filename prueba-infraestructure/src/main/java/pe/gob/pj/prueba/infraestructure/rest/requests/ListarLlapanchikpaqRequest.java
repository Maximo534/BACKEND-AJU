package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarLlapanchikpaqRequest implements Serializable {

    // ✅ BUSCADOR GENERAL (ID, Lugar, Nombre Corte)
    private String search;

    // ✅ FILTRO COMBO
    private String distritoJudicialId;

    // ✅ FILTRO RANGO DE FECHAS (Sobre f_inicio)
    private LocalDate fechaInicio; // Desde
    private LocalDate fechaFin;    // Hasta
}