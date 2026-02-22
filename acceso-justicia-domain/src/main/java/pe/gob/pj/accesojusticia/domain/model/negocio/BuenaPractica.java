package pe.gob.pj.accesojusticia.domain.model.negocio;

import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.model.Auditoria;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BuenaPractica extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    // --- Maestros ---
    Long distritoJudicialId;
    String distritoJudicialNombre;

    // --- Contacto ---
    String responsable;
    String email;
    String telefono;
    String integrantes;

    // --- General ---
    LocalDate fechaInicio;
    LocalDate fechaFin;
    String titulo;
    String categoria;

    // --- ANÁLISIS ---
    String problema;
    String causa;
    String consecuencia;
    String descripcionGeneral;

    // --- PLANIFICACIÓN ---
    String logro;
    String objetivo;
    String aliado;
    String dificultad;
    String norma;

    // --- EJECUCIÓN ---
    String desarrollo;
    String ejecucion;
    String actividad;

    // --- RESULTADOS ---
    String aporte;
    String resultado;
    String impacto;
    String publicoObjetivo;
    String leccionAprendida;
    String infoAdicional;

    // --- CAMPOS ADICIONALES  ---
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

    // --- Auditoría Negocio ---
    LocalDate fechaRegistro;
    Long usuarioRegistroId;
    String activo;

    // --- Archivos ---
    List<Archivo> archivosGuardados;
}