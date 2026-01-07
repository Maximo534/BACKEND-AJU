package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarBuenaPracticaRequest implements Serializable {

    private String id;
    private String distritoJudicialId;

    private String responsable;
    private String email;
    private String telefono;
    private String integrantes;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    private String titulo;
    private String categoria;

    private String problema;
    private String causa;
    private String consecuencia;

    private String descripcionGeneral;
    private String logro;
    private String objetivo;
    private String aliado;
    private String dificultad;
    private String norma;

    private String desarrollo;
    private String ejecucion;
    private String actividad;

    private String aporte;
    private String resultado;
    private String impacto;
    private String publicoObjetivo;

    private String leccionAprendida;
    private String infoAdicional;
}