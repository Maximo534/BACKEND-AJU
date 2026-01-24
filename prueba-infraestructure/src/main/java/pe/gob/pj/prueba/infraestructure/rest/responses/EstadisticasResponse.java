package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstadisticasResponse implements Serializable {

    private Integer anioConsultado;

    private GraficoResponse chartTopMagistrados;

    private GraficoResponse chartPorEje;
    private GraficoResumenMagistradoResponse chartResumenMagistrados;
    private GraficoResponse chartTopDistrito;
    private GraficoEvolucionMensualResponse chartEvolucionMensual;

}