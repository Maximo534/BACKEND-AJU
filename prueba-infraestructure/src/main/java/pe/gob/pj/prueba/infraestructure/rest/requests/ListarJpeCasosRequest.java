package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarJpeCasosRequest implements Serializable {

    static final long serialVersionUID = 1L;

    String search;

    Long distritoJudicialId;
    Long ugelId;
    Long institucionEducativaId;

    LocalDate fechaRegistro;
}