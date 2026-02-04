package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mov_buena_practica", schema = EsquemaConstants.PRUEBA)
public class MovBuenaPracticaEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_BUENA_PRACT", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_buena_practica", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_BUENA_PRACT")
    @Column(name = "n_buena_pract_id", nullable = false)
    Long id; 

    @Column(name = "c_codigo", length = 17, nullable = false, unique = true)
    String codigo;

    @Column(name = "n_distrito_jud_id", nullable = false)
    Long distritoJudicialId;

    // --- CONTACTO ---
    @Column(name = "x_responsable", length = 80) String responsable;
    @Column(name = "x_email", length = 80) String email;
    @Column(name = "x_telefono", length = 9) String telefono;
    @Column(name = "t_integrante", columnDefinition = "TEXT") String integrantes;

    // --- GENERAL ---
    @Column(name = "f_inicio") LocalDate fechaInicio;
    @Column(name = "f_fin") LocalDate fechaFin;
    @Column(name = "x_titulo", length = 200) String titulo;
    @Column(name = "x_categoria", length = 150) String categoria;

    // --- ANÁLISIS ---
    @Column(name = "t_problema", columnDefinition = "TEXT") String problema;
    @Column(name = "t_causa", columnDefinition = "TEXT") String causa;
    @Column(name = "t_consecuencia", columnDefinition = "TEXT") String consecuencia;
    @Column(name = "t_desc_general", columnDefinition = "TEXT") String descripcionGeneral;

    // --- PLANIFICACIÓN ---
    @Column(name = "t_logro", columnDefinition = "TEXT") String logro;
    @Column(name = "t_objetivo", columnDefinition = "TEXT") String objetivo;
    @Column(name = "t_aliado", columnDefinition = "TEXT") String aliado;
    @Column(name = "t_dificultad", columnDefinition = "TEXT") String dificultad;
    @Column(name = "t_norma", columnDefinition = "TEXT") String norma;

    // --- EJECUCIÓN ---
    @Column(name = "t_desarrollo", columnDefinition = "TEXT") String desarrollo;
    @Column(name = "t_ejecucion", columnDefinition = "TEXT") String ejecucion;
    @Column(name = "t_actividad", columnDefinition = "TEXT") String actividad;

    // --- RESULTADOS ---
    @Column(name = "t_aporte", columnDefinition = "TEXT") String aporte;
    @Column(name = "t_resultado", columnDefinition = "TEXT") String resultado;
    @Column(name = "t_impacto", columnDefinition = "TEXT") String impacto;
    @Column(name = "t_publico_obj", columnDefinition = "TEXT") String publicoObjetivo;
    @Column(name = "t_leccion_aprendida", columnDefinition = "TEXT") String leccionAprendida;
    @Column(name = "t_info_adicional", columnDefinition = "TEXT") String infoAdicional;

    // --- CAMPOS ADICIONALES (Evaluación) ---
    @Column(name = "t_aporte_relev", columnDefinition = "TEXT") String aporteRelevante;
    @Column(name = "t_situac_anter", columnDefinition = "TEXT") String situacionAnterior;
    @Column(name = "t_situac_desp", columnDefinition = "TEXT") String situacionDespues;
    @Column(name = "t_impact_princ", columnDefinition = "TEXT") String impactoPrincipal;
    @Column(name = "t_mejora", columnDefinition = "TEXT") String mejora;
    @Column(name = "t_posib_relica", columnDefinition = "TEXT") String posibilidadReplica;
    @Column(name = "t_acciones", columnDefinition = "TEXT") String acciones;
    @Column(name = "t_objet_institu", columnDefinition = "TEXT") String objInstitucional;
    @Column(name = "t_polit_public", columnDefinition = "TEXT") String politicaPublica;
    @Column(name = "t_importancia", columnDefinition = "TEXT") String importancia;
    @Column(name = "t_aspec_implemen", columnDefinition = "TEXT") String aspectosImplementacion;
    @Column(name = "t_aporte_sociedad", columnDefinition = "TEXT") String aporteSociedad;
    @Column(name = "t_medidas", columnDefinition = "TEXT") String medidas;
    @Column(name = "t_norma_interna", columnDefinition = "TEXT") String normaInterna;
    @Column(name = "t_dific_interna", columnDefinition = "TEXT") String dificInterna;
    @Column(name = "t_dific_externa", columnDefinition = "TEXT") String dificExterna;
    @Column(name = "t_aliado_ext", columnDefinition = "TEXT") String aliadoExt;
    @Column(name = "t_aliado_int", columnDefinition = "TEXT") String aliadoInt;

    // --- Auditoría ---
    @Column(name = "f_reg_activ", nullable = false)
    LocalDate fechaRegistroActividad = LocalDate.now();

    @Column(name = "n_usuario_reg_id", nullable = false)
    Long usuarioRegistroId;

    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    @Column(name = "f_registro", insertable = false, updatable = false)
    LocalDateTime fRegistro;

    @Column(name = "f_aud")
    LocalDateTime fAud = LocalDateTime.now();

    @Column(name = "b_aud")
    String bAud = OperacionBaseDatos.INSERTAR.getNombre();

    @Column(name = "c_aud_uid") String cAudId;
    @Column(name = "c_aud_uidred") String cAudIdRed = InformacionRedUtils.getNombreRed();
    @Column(name = "c_aud_pc") String cAudPc = InformacionRedUtils.getPc();
    @Column(name = "c_aud_ip") String cAudIp = InformacionRedUtils.getIp();
    @Column(name = "c_aud_mcaddr") String cAudMcAddr = InformacionRedUtils.getMac();
}