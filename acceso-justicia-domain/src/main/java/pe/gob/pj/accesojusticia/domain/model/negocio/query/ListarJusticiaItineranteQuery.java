package pe.gob.pj.accesojusticia.domain.model.negocio.query;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarJusticiaItineranteQuery {
    String search;
    Long distritoJudicialId;
    LocalDate fechaInicio;
    LocalDate fechaFin;

    String usuarioRegistroLogin;
}