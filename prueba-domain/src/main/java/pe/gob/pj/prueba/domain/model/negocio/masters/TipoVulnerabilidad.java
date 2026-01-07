package pe.gob.pj.prueba.domain.model.negocio.masters;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TipoVulnerabilidad {
    private Integer id;
    private String descripcion;
}