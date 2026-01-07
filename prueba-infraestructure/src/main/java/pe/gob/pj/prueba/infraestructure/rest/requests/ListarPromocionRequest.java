package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ListarPromocionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigoRegistro;
    private String descripcionActividad;
    private String distritoJudicialId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}