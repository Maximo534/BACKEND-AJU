package pe.gob.pj.accesojusticia.domain.model.negocio;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dashboard implements Serializable {

    static final long serialVersionUID = 1L;

    Integer anioConsultado;
    String usuarioConsultado;

    DetalleGrafico anualJusticiaItinerante;
    DetalleGrafico anualFortalecimiento;
    DetalleGrafico anualPromocion;

    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleGrafico implements Serializable {
        static final long serialVersionUID = 1L;
        List<String> labels;
        List<Integer> cantidad;
    }
}