package pe.gob.pj.accesojusticia.domain.model.negocio.query;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarJuezEscolarQuery {
    String search;
    Long distritoJudicialId;
    Long ugelId;
    Long institucionEducativaId; 
}