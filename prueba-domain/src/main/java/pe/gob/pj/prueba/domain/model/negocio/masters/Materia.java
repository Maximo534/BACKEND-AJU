package pe.gob.pj.prueba.domain.model.negocio.masters;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class Materia {
    private Integer id;
    private String descripcion;
}