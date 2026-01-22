package pe.gob.pj.prueba.domain.model.negocio;

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
public class Dashboard implements Serializable {

    private Integer anioConsultado;
    private String usuarioConsultado;
    private GraficoBarras graficoAnual;

    @Data
    @Builder
    public static class GraficoBarras implements Serializable {
        private List<DataMes> justiciaItinerante;
        private List<DataMes> fortalecimiento;
        private List<DataMes> promocionCultura;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataMes implements Serializable {
        private Integer mes;
        private Integer cantidad;
    }
}
