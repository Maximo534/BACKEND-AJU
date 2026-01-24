package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class GraficoResponse implements Serializable {
    private List<String> labels;
    private List<Integer> cantidad;
}