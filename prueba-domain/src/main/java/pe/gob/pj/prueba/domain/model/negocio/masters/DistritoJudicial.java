package pe.gob.pj.prueba.domain.model.negocio.masters;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DistritoJudicial {
    private String id;
    private String descripcion;
}