package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class DocumentoResponse implements Serializable {
    private String id;
    private String nombre;
    private String tipo;
    private Integer periodo;
    private String formato;
    private Integer categoriaId;
    private String urlDescarga;
}