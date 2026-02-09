package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class UsuarioSesionResponse implements Serializable {
    private String usuario;
    private String nombreCompleto;
    private String cargo;
    private String sede;
    private String eje;
}