package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
        private List<DataMesResponse> justiciaItinerante;
        private List<DataMesResponse> fortalecimiento;
        private List<DataMesResponse> promocionCultura;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataMesResponse implements Serializable {
        private Integer mes;
        private Integer cantidad;
    }
}
