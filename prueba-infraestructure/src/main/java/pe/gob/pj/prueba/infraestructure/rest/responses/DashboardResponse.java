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

    private DetalleGraficoResponse anualJusticiaItinerante;
    private DetalleGraficoResponse anualFortalecimiento;
    private DetalleGraficoResponse anualPromocion;

    @Data
    @Builder
    public static class DetalleGraficoResponse implements Serializable {
        private List<String> labels;
        private List<Integer> cantidad;
    }
}