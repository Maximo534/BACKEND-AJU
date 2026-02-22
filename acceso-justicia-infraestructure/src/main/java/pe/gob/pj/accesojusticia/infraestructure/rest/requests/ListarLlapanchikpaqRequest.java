package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarLlapanchikpaqRequest implements Serializable {

    static final long serialVersionUID = 1L;

    String search;

    Long distritoJudicialId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaFin;
}