package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BuenaPracticaResponse implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;
    Long distritoJudicialId;
    String distritoJudicialNombre;
    String titulo;
    LocalDate fechaInicio;
    LocalDate fechaFin;
    String categoria;
    String estado;

    // --- CONTACTO ---
    String responsable;
    String email;
    String telefono;
    String integrantes;

    // --- DETALLE COMPLETO ---
    String problema;
    String causa;
    String consecuencia;
    String descripcionGeneral;
    String logro;
    String objetivo;
    String aliado;
    String dificultad;
    String norma;

    String desarrollo;
    String ejecucion;
    String actividad;

    String aporte;
    String resultado;
    String impacto;
    String publicoObjetivo;

    String leccionAprendida;
    String infoAdicional;

    // --- CAMPOS EXPUESTOS---
    String aporteRelevante;
    String situacionAnterior;
    String situacionDespues;
    String impactoPrincipal;
    String mejora;
    String posibilidadReplica;
    String acciones;
    String objInstitucional;
    String politicaPublica;
    String importancia;
    String aspectosImplementacion;
    String aporteSociedad;
    String medidas;
    String normaInterna;
    String dificInterna;
    String dificExterna;
    String aliadoExt;
    String aliadoInt;

    // --- Auditoría ---
    LocalDate fechaRegistro;
    String usuarioRegistro;

    // --- ARCHIVOS ---
    List<Archivo> archivos;
}