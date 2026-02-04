package pe.gob.pj.prueba.domain.model.negocio.query;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarJpeCasosQuery {

    String search;
    Long distritoJudicialId;
    Long ugelId;
    Long institucionEducativaId;
    LocalDate fechaRegistro;
}