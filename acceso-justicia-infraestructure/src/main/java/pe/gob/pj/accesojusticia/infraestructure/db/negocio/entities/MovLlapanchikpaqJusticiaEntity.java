package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.common.enums.Estado;
import pe.gob.pj.accesojusticia.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.accesojusticia.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.accesojusticia.infraestructure.common.utils.InformacionRedUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mov_llapanchikpaq_justicia", schema = EsquemaConstants.PRUEBA)
public class MovLlapanchikpaqJusticiaEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_LLJ", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_llapanchikpaq_justicia", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_LLJ")
    @Column(name = "n_llj_id", nullable = false)
    Long id;

    @Column(name = "c_codigo", length = 17, nullable = false, unique = true)
    String codigo;

    @Column(name = "n_distrito_jud_id", nullable = false)
    Long distritoJudicialId;

    @Column(name = "f_inicio", nullable = false)
    LocalDate fechaInicio;

    @Column(name = "f_fin", nullable = false)
    LocalDate fechaFin;

    @Column(name = "x_res_plan_anual", length = 50)
    String resolucionPlanAnual;

    @Column(name = "x_res_admin_plan", length = 50)
    String resolucionAdminPlan;

    @Column(name = "x_doc_autoriza", length = 60)
    String documentoAutoriza;

    @Column(name = "x_lugar_actv", length = 200)
    String lugarActividad;

    // --- Ubicación (Long) ---
    @Column(name = "n_depa_id") Long departamentoId;
    @Column(name = "n_prov_id") Long provinciaId;
    @Column(name = "n_dist_id") Long distritoGeograficoId;

    @Column(name = "n_eje_id", nullable = false)
    Long ejeId;

    @Column(name = "x_desc_activ", columnDefinition = "TEXT")
    String descripcionActividad;

    @Column(name = "x_inst_aliada", columnDefinition = "TEXT")
    String institucionesAliadas;

    @Column(name = "x_observacion", columnDefinition = "TEXT")
    String observacion;

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

    // --- RELACIONES  ---

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "n_llj_id", referencedColumnName = "n_llj_id", nullable = false)
    List<MovLljPersonasBeneficiadasEntity> beneficiadas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "n_llj_id", referencedColumnName = "n_llj_id", nullable = false)
    List<MovLljPersonasAtendidasEntity> atendidas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "n_llj_id", referencedColumnName = "n_llj_id", nullable = false)
    List<MovLljCasosAtendidosEntity> casos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "n_llj_id", referencedColumnName = "n_llj_id", nullable = false)
    List<MovLljTareaRealizadasEntity> tareas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.id != null) {
            if (beneficiadas != null) beneficiadas.forEach(c -> c.setLljId(this.id));
            if (atendidas != null) atendidas.forEach(c -> c.setLljId(this.id));
            if (casos != null) casos.forEach(c -> c.setLljId(this.id));
            if (tareas != null) tareas.forEach(c -> c.setLljId(this.id));
        }
    }
}