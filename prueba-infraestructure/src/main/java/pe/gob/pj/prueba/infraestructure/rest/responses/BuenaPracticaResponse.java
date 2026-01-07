package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import pe.gob.pj.prueba.domain.model.negocio.Archivo; // O usa un ArchivoResponse si prefieres
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class BuenaPracticaResponse implements Serializable {

    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private String titulo;
    private String estado; // (Tu campo 'activo' o calculado)

    // --- TODOS LOS CAMPOS QUE FALTABAN ---
    private String responsable;
    private String email;
    private String telefono;
    private LocalDate fechaInicio;
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

    private String usuarioRegistro;

    private List<Archivo> archivosGuardados;
}