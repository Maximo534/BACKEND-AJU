package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Documento implements Serializable {

    private String id;
    private String nombre;
    private String tipo;
    private String formato;
    private String rutaArchivo;
    private Integer periodo;
    private String activo;
    private Integer categoriaId;
    // Campo auxiliar para el Frontend (No está en BD, se genera al vuelo)
    private String urlDescarga;
}