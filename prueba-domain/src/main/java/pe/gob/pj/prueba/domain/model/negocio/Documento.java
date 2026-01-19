package pe.gob.pj.prueba.domain.model.negocio;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class Documento implements Serializable {
    private String id;
    private String nombre;
    private String tipo;
    private String formato;
    private String rutaArchivo;
    private Integer periodo;
    private String activo;
    private Integer categoriaId;

}