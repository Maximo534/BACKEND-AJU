package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class JusticiaItineranteResponse implements Serializable {
    private String id;               // CÓDIGO DE REGISTRO
    private String publicoObjetivo;  // PÚBLICO OBJETIVO
    private String lugar;            // Para mostrar info extra
    private LocalDate fechaInicio;   // DURACIÓN
    private LocalDate fechaFin;      // DURACIÓN
    private LocalDate fechaRegistro; // REGISTRADO
    private String estado;           // Si tiene archivos o no (opcional)
}