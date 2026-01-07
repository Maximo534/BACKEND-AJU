package pe.gob.pj.prueba.domain.model.negocio;

import lombok.Data;
import java.io.Serializable;

@Data
public class DetallePersonasAtendidas implements Serializable {
    private Integer tipoVulnerabilidadId;
    private String rangoEdad;
    private Integer cantFemenino;
    private Integer cantMasculino;
    private Integer cantLgtbiq;
}