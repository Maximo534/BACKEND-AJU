package pe.gob.pj.accesojusticia.domain.model.negocio;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data @Builder
public class ResumenEstadistico implements Serializable {
    private String etiqueta;
    private Long cantidad;
}