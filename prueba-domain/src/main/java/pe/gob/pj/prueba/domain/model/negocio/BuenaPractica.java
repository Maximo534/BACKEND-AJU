package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BuenaPractica implements Serializable {
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private String responsable;
    private String email;
    private String telefono;
    private String integrantes;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String titulo;
    private String categoria;

    // Campos largos
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

    private String usuarioRegistro;

    private String search;
    private List<Archivo> archivosGuardados;
}