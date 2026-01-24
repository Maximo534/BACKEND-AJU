package pe.gob.pj.prueba.domain.model.negocio;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstadisticasData {
    private Integer anio;
    private DetalleGrafico chartTopMagistrados;
    private DetalleGrafico chartPorEje;
    private ResumenMagistrado chartResumenMagistrados;
    private DetalleGrafico chartTopDistrito;
    private EvolucionMensual chartEvolucionMensual;
}