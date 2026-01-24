package pe.gob.pj.prueba.domain.model.negocio;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DetalleGrafico {
    private List<String> labels;
    private List<Integer> cantidad;
}