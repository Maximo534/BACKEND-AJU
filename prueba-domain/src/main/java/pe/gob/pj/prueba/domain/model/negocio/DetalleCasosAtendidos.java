package pe.gob.pj.prueba.domain.model.negocio;

import lombok.Data;
import java.io.Serializable;

@Data
public class DetalleCasosAtendidos implements Serializable {
    private Integer materiaId;
    private Integer numDemandas;
    private Integer numAudiencias;
    private Integer numSentencias;
    private Integer numProcesos;
    private Integer numNotificaciones;
    private Integer numOrientaciones;
}