package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarJuezEscolarRequest implements Serializable {

    static final long serialVersionUID = 1L;

    String search;

    Long distritoJudicialId;
    Long ugelId;
    Long institucionEducativaId;
}