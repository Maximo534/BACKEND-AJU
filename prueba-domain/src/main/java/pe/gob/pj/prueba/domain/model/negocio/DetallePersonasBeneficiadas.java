package pe.gob.pj.prueba.domain.model.negocio;
import lombok.Data;
import java.io.Serializable;

@Data
public class DetallePersonasBeneficiadas implements Serializable {
    private String descripcionRango;
    private String codigoRango;
    private Integer cantFemenino;
    private Integer cantMasculino;
    private Integer cantLgtbiq;
}