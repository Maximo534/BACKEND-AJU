package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentoResponse implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String nombre;
    String tipo;
    Integer periodo;
    String formato;

    Long categoriaDocumentoId;
    String categoriaNombre;
}