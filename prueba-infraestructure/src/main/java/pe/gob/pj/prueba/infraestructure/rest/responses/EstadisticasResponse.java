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
public class EstadisticasResponse implements Serializable {

    static final long serialVersionUID = 1L;

    Integer anioConsultado;

    GraficoSimpleResponse chartTopMagistrados;
    GraficoSimpleResponse chartPorEje;
    GraficoSimpleResponse chartTopDistrito;

    // Gráficos Multiseries
    GraficoMultiserieResponse chartResumenMagistrados;
    GraficoMultiserieResponse chartEvolucionMensual;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GraficoSimpleResponse implements Serializable {
        static final long serialVersionUID = 1L;
        List<String> labels;
        List<Integer> cantidad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GraficoMultiserieResponse implements Serializable {
        static final long serialVersionUID = 1L;
        List<String> labels;
        List<Integer> dataJusticia;
        List<Integer> dataCultura;
        List<Integer> dataFortalecimiento;
    }
}