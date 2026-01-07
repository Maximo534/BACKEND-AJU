package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarBuenaPracticaRequest implements Serializable {

    private String codigo;    // Para buscar por ID
    private String titulo;    // Para buscar por nombre
    private String distritoJudicialId; // Para filtrar por corte

    // RANGO DE FECHAS (El usuario selecciona "Desde" y "Hasta")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin; // <--- AQUÍ ESTÁ EL CAMPO QUE NECESITAS
}