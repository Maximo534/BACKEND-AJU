package pe.gob.pj.prueba.domain.model.negocio.masters;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class Tambo {
    private String id;
    private String nombre;
}