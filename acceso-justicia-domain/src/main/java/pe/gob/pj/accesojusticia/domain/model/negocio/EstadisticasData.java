package pe.gob.pj.accesojusticia.domain.model.negocio;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstadisticasData implements Serializable {

    static final long serialVersionUID = 1L;

    Integer anio;

    DetalleGrafico chartTopMagistrados;
    DetalleGrafico chartPorEje;
    ResumenMagistrado chartResumenMagistrados;
    DetalleGrafico chartTopDistrito;
    EvolucionMensual chartEvolucionMensual;


    @Data @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleGrafico implements Serializable {
        static final long serialVersionUID = 1L;
        List<String> labels;
        List<Integer> cantidad;
    }

    @Data @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ResumenMagistrado implements Serializable {
        static final long serialVersionUID = 1L;
        List<String> labels;
        List<Integer> dataJusticia;
        List<Integer> dataCultura;
        List<Integer> dataFortalecimiento;
    }

    @Data @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EvolucionMensual implements Serializable {
        static final long serialVersionUID = 1L;
        List<String> labels;
        List<Integer> dataJusticia;
        List<Integer> dataCultura;
        List<Integer> dataFortalecimiento;
    }
}