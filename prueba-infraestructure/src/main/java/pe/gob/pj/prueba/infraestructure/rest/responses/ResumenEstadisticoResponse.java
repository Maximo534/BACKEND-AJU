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

    //(ej: "LIMA", "CUSCO")
    private String etiqueta;

    private Long cantidad;
}