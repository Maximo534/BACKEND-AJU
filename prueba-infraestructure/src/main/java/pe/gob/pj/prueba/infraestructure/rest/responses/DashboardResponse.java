package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardResponse implements Serializable {

    static final long serialVersionUID = 1L;

    Integer anioConsultado;
    String usuarioConsultado;

    DetalleGraficoResponse anualJusticiaItinerante;
    DetalleGraficoResponse anualFortalecimiento;
    DetalleGraficoResponse anualPromocion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleGraficoResponse implements Serializable {
        static final long serialVersionUID = 1L;

        List<String> labels;
        List<Integer> cantidad;
    }
}