package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListarPromocionRequest implements Serializable {

    static final long serialVersionUID = 1L;

    String search;

    // Filtros
    Long distritoJudicialId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaFin;
}