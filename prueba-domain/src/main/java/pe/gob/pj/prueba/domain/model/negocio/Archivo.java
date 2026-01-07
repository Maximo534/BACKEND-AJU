package pe.gob.pj.prueba.domain.model.negocio;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Archivo {
    private String nombre;
    private String tipo;
    private String ruta;
    private String numeroIdentificacion;
}