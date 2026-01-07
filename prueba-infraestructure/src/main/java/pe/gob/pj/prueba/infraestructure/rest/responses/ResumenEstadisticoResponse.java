package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenEstadisticoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    // Nombre de la categoría (ej: "LIMA", "CUSCO")
    private String etiqueta;

    // Valor numérico (ej: 45, 10)
    private Long cantidad;
}