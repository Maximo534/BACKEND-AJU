package pe.gob.pj.prueba.domain.model.negocio.masters;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class Plan {
    private String id;
    private String descripcion;
    private String periodo;
    private String resolucionGerencia;
    private String resolucionAprobacion;
}