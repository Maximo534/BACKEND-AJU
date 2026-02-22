package pe.gob.pj.accesojusticia.domain.model.negocio;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ResumenMagistrado {
    private List<String> labels;
    private List<Integer> dataJusticia;
    private List<Integer> dataCultura;
    private List<Integer> dataFortalecimiento;
}