package pe.gob.pj.prueba.domain.model.negocio.query;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarPromocionQuery {

    String search;
    Long distritoJudicialId;
    LocalDate fechaInicio;
    LocalDate fechaFin;
}