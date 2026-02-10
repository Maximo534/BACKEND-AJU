package pe.gob.pj.prueba.domain.model.negocio.query;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarFortalecimientoQuery {

    String search;
    Long distritoJudicialId;
    String tipoEvento;
    LocalDate fechaInicio;
    LocalDate fechaFin;

    String usuarioRegistroLogin;
}