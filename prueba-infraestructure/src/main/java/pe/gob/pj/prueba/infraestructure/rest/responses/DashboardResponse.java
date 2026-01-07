package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse implements Serializable {

    private Integer anioConsultado;
    private String usuarioConsultado;
    private GraficoBarrasResponse graficoAnual;

    @Data
    @Builder
    public static class GraficoBarrasResponse implements Serializable {
        private List<Integer> justiciaItinerante;
        private List<Integer> fortalecimiento;
        private List<Integer> promocionCultura;
    }
}