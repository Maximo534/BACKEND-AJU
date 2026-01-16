package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuenaPracticaResponse implements Serializable {

    // --- CABECERA ---
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private String titulo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String categoria;

    // --- CONTACTO ---
    private String responsable;
    private String email;
    private String telefono;
    private String integrantes;

    // --- DETALLE COMPLETO ---
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

    // --- CAMPOS EXPUESTOS (Antes ocultos) ---
    private String aporteRelevante;
    private String situacionAnterior;
    private String situacionDespues;
    private String impactoPrincipal;
    private String mejora;
    private String posibilidadReplica;
    private String acciones;
    private String objInstitucional;
    private String politicaPublica;
    private String importancia;
    private String aspectosImplementacion;
    private String aporteSociedad;
    private String medidas;
    private String normaInterna;
    private String dificInterna;
    private String dificExterna;
    private String aliadoExt;
    private String aliadoInt;

    // --- ARCHIVOS ---
    private List<Archivo> archivos;
}