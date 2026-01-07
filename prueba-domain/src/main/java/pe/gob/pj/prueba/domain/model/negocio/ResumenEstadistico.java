package pe.gob.pj.prueba.domain.model.negocio;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data @Builder
public class ResumenEstadistico implements Serializable {
    private String etiqueta; // Nombre de la Corte
    private Long cantidad;   // Valor numérico
}