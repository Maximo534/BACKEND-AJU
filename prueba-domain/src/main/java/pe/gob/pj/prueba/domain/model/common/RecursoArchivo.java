package pe.gob.pj.prueba.domain.model.common;

import lombok.Builder;
import lombok.Data;
import java.io.InputStream;

@Data
@Builder
public class RecursoArchivo {
    private InputStream stream;
    private String nombreFileName;
}