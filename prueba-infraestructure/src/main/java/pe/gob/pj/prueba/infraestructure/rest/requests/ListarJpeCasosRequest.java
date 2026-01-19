package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarJpeCasosRequest implements Serializable {

    // (ID o Resumen)
    private String search;

    private String distritoJudicialId;
    private String ugelId;
    private String institucionEducativaId;

    //FILTRO EXACTO POR FECHA
    private LocalDate fechaRegistro;
}