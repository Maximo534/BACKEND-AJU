package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarFfcRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigoRegistro;      // Para filtrar por ID (num_even)
    private String nombreEvento;        // Para filtrar por Nombre del Evento
    private String distritoJudicialId;  // Para filtrar por Corte
    private LocalDate fechaInicio;      // Rango Desde
    private LocalDate fechaFin;         // Rango Hasta
}