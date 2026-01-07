package pe.gob.pj.prueba.domain.model.negocio;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class DetalleTareaRealizada implements Serializable {
    private String tareaId;
    private LocalDate fechaInicio;
}