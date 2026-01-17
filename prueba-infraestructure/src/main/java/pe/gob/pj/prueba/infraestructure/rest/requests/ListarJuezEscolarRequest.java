package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;

@Data
public class ListarJuezEscolarRequest implements Serializable {

    // Buscador general (DNI, Nombres, Resolución)
    private String search;

    // Filtros jerárquicos
    private String distritoJudicialId;
    private String ugelId;
    private String institucionEducativaId;
}