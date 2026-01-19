package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;

@Data
public class ListarJuezEscolarRequest implements Serializable {

    //  (DNI, Nombres, Resolución)
    private String search;

    private String distritoJudicialId;
    private String ugelId;
    private String institucionEducativaId;
}