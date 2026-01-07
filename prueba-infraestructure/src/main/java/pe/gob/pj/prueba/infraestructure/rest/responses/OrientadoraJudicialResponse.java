package pe.gob.pj.prueba.infraestructure.rest.responses;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data @Builder
public class OrientadoraJudicialResponse implements Serializable {
    private String id;
    private String nombrePersona;
    private String numeroExpediente;
    private String estado;
}