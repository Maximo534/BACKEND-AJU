package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "mov_aju_buena_practicas")
public class MovBuenaPracticaEntity implements Serializable {

    @Id
    @Column(name = "c_buena_pract_id", length = 17)
    private String id;

    @Column(name = "c_distrito_jud_id", length = 2)
    private String distritoJudicialId;

    // --- CONTACTO ---
    @Column(name = "x_responsable", length = 80) private String responsable;
    @Column(name = "x_email", length = 80) private String email;
    @Column(name = "x_telefono", length = 9) private String telefono;
    @Column(name = "t_integrante", columnDefinition = "TEXT") private String integrantes;

    // --- GENERAL ---
    @Column(name = "f_inicio") private LocalDate fechaInicio;
    @Column(name = "x_titulo", length = 200) private String titulo;
    @Column(name = "x_categoria", length = 150) private String categoria;

    // --- ANÁLISIS ---
    @Column(name = "t_problema", columnDefinition = "TEXT") private String problema;
    @Column(name = "t_causa", columnDefinition = "TEXT") private String causa;
    @Column(name = "t_consecuencia", columnDefinition = "TEXT") private String consecuencia;
    @Column(name = "t_desc_general", columnDefinition = "TEXT") private String descripcionGeneral;

    // --- PLANIFICACIÓN ---
    @Column(name = "t_logro", columnDefinition = "TEXT") private String logro;
    @Column(name = "t_objetivo", columnDefinition = "TEXT") private String objetivo;
    @Column(name = "t_aliado", columnDefinition = "TEXT") private String aliado;
    @Column(name = "t_dificultad", columnDefinition = "TEXT") private String dificultad;
    @Column(name = "t_norma", columnDefinition = "TEXT") private String norma;

    // --- EJECUCIÓN ---
    @Column(name = "t_desarrollo", columnDefinition = "TEXT") private String desarrollo;
    @Column(name = "t_ejecucion", columnDefinition = "TEXT") private String ejecucion;
    @Column(name = "t_actividad", columnDefinition = "TEXT") private String actividad;

    // --- RESULTADOS ---
    @Column(name = "t_aporte", columnDefinition = "TEXT") private String aporte; // Innovación
    @Column(name = "t_resultado", columnDefinition = "TEXT") private String resultado;
    @Column(name = "t_impacto", columnDefinition = "TEXT") private String impacto;
    @Column(name = "t_publico_obj", columnDefinition = "TEXT") private String publicoObjetivo;
    @Column(name = "t_leccion_aprendida", columnDefinition = "TEXT") private String leccionAprendida;
    @Column(name = "t_info_adicional", columnDefinition = "TEXT") private String infoAdicional;

    // --- CAMPOS FANTASMA (Obligatorios en BD pero que no envías desde el Front) ---
    // Los definimos para que Hibernate sepa que existen, pero los llenaremos auto.
    @Column(name = "t_aporte_relev", columnDefinition = "TEXT") private String aporteRelevante;
    @Column(name = "t_situac_anter", columnDefinition = "TEXT") private String situacionAnterior;
    @Column(name = "t_situac_desp", columnDefinition = "TEXT") private String situacionDespues;
    @Column(name = "t_impact_princ", columnDefinition = "TEXT") private String impactoPrincipal;
    @Column(name = "t_mejora", columnDefinition = "TEXT") private String mejora;
    @Column(name = "t_posib_relica", columnDefinition = "TEXT") private String posibilidadReplica;
    @Column(name = "t_acciones", columnDefinition = "TEXT") private String acciones;
    @Column(name = "t_objet_institu", columnDefinition = "TEXT") private String objInstitucional;
    @Column(name = "t_polit_public", columnDefinition = "TEXT") private String politicaPublica;
    @Column(name = "t_importancia", columnDefinition = "TEXT") private String importancia;
    @Column(name = "t_aspec_implemen", columnDefinition = "TEXT") private String aspectosImplementacion;
    @Column(name = "t_aporte_sociedad", columnDefinition = "TEXT") private String aporteSociedad;
    @Column(name = "t_medidas", columnDefinition = "TEXT") private String medidas;
    @Column(name = "t_norma_interna", columnDefinition = "TEXT") private String normaInterna;
    @Column(name = "t_dific_interna", columnDefinition = "TEXT") private String dificInterna;
    @Column(name = "t_dific_externa", columnDefinition = "TEXT") private String dificExterna;
    @Column(name = "t_aliado_ext", columnDefinition = "TEXT") private String aliadoExt;
    @Column(name = "t_aliado_int", columnDefinition = "TEXT") private String aliadoInt;


    // Auditoría
    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;

    // --- MAGIA AQUÍ: Llenar por defecto los campos obligatorios vacíos ---
    @PrePersist
    @PreUpdate
    public void prePersist() {
        String DEFAULT_TEXT = "-";

        if (this.aporteRelevante == null) this.aporteRelevante = DEFAULT_TEXT;
        if (this.situacionAnterior == null) this.situacionAnterior = DEFAULT_TEXT;
        if (this.situacionDespues == null) this.situacionDespues = DEFAULT_TEXT;
        if (this.impactoPrincipal == null) this.impactoPrincipal = DEFAULT_TEXT;
        if (this.mejora == null) this.mejora = DEFAULT_TEXT;
        if (this.posibilidadReplica == null) this.posibilidadReplica = DEFAULT_TEXT;
        if (this.acciones == null) this.acciones = DEFAULT_TEXT;
        if (this.objInstitucional == null) this.objInstitucional = DEFAULT_TEXT;
        if (this.politicaPublica == null) this.politicaPublica = DEFAULT_TEXT;
        if (this.importancia == null) this.importancia = DEFAULT_TEXT;
        if (this.aspectosImplementacion == null) this.aspectosImplementacion = DEFAULT_TEXT;
        if (this.aporteSociedad == null) this.aporteSociedad = DEFAULT_TEXT;
        if (this.medidas == null) this.medidas = DEFAULT_TEXT;
        if (this.normaInterna == null) this.normaInterna = DEFAULT_TEXT;
        if (this.dificInterna == null) this.dificInterna = DEFAULT_TEXT;
        if (this.dificExterna == null) this.dificExterna = DEFAULT_TEXT;
        if (this.aliadoExt == null) this.aliadoExt = DEFAULT_TEXT;
        if (this.aliadoInt == null) this.aliadoInt = DEFAULT_TEXT;

        // También protegemos los campos normales por si el front envía nulls
        if (this.aliado == null) this.aliado = DEFAULT_TEXT;
        if (this.norma == null) this.norma = DEFAULT_TEXT;
    }
}