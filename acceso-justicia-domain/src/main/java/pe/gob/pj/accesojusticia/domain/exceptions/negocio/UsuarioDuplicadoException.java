package pe.gob.pj.accesojusticia.domain.exceptions.negocio;

import java.io.Serial;
import java.io.Serializable;

public class UsuarioDuplicadoException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public UsuarioDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public UsuarioDuplicadoException() {
        super("El usuario ya existe en el sistema.");
    }
}