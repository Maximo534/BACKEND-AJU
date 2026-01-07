package pe.gob.pj.prueba.domain.model.negocio;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard implements Serializable {

    private Integer anioConsultado;
    private String usuarioConsultado;

    private GraficoBarras graficoAnual;

    @Data
    @Builder
    public static class GraficoBarras implements Serializable {
        private List<Integer> justiciaItinerante;
        private List<Integer> fortalecimiento;
        private List<Integer> promocionCultura;
    }
}