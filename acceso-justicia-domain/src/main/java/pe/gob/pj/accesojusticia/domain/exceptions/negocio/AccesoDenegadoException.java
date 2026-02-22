package pe.gob.pj.accesojusticia.domain.exceptions.negocio;

import java.io.Serial;
import java.io.Serializable;

public class AccesoDenegadoException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }

    public AccesoDenegadoException() {
        super("No tiene permisos para realizar esta operación.");
    }
}