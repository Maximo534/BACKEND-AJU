package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Entity
@Table(name = "mov_aju_llapanchikpaq_justicia")
public class MovLlapanchikpaqJusticia implements Serializable {

    @Id
    @Column(name = "c_llj_id", length = 17)
    private String id;

    @Column(name = "c_distrito_jud_id", length = 2)
    private String distritoJudicialId;

    @Column(name = "f_inicio")
    private LocalDate fechaInicio;

    @Column(name = "x_res_plan_anual", length = 50)
    private String resolucionPlanAnual;

    @Column(name = "x_res_admin_plan", length = 50)
    private String resolucionAdminPlan;

    @Column(name = "x_doc_autoriza", length = 60)
    private String documentoAutoriza;

    @Column(name = "x_lugar_activ", length = 150)
    private String lugarActividad;

    @Column(name = "c_depa_id", length = 2)
    private String departamentoId;
    @Column(name = "c_prov_id", length = 4)
    private String provinciaId;
    @Column(name = "c_dist_id", length = 6)
    private String distritoGeograficoId;

    @Column(name = "n_num_muj_indg")
    private Integer numMujeresIndigenas;
    @Column(name = "n_per_quech_aymar")
    private Integer numPersonasQuechuaAymara;
    @Column(name = "n_juez_quech_aymar")
    private Integer numJuecesQuechuaAymara;

    @Column(name = "l_uso_leng_nativa", length = 2)
    private String usoLenguaNativa;
    @Column(name = "x_leng_nativa", length = 25)
    private String lenguaNativaDesc;

    @Column(name = "t_derivacion", columnDefinition = "TEXT")
    private String derivacion;
    @Column(name = "t_impac_activ", columnDefinition = "TEXT")
    private String impactoActividad;
    @Column(name = "t_observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "f_reg_activ")
    private LocalDate fechaRegistro;
    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;
    @Column(name = "l_activo", length = 1)
    private String activo;

    // --- RELACIONES CON HIJOS RENOMBRADOS ---

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_llj_id", referencedColumnName = "c_llj_id", insertable = false, updatable = false)
    private List<MovLljPersonasBeneficiadasEntity> beneficiadas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_llj_id", referencedColumnName = "c_llj_id", insertable = false, updatable = false)
    private List<MovLljPersonasAtendidasEntity> atendidas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_llj_id", referencedColumnName = "c_llj_id", insertable = false, updatable = false)
    private List<MovLljCasosAtendidosEntity> casos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_llj_id", referencedColumnName = "c_llj_id", insertable = false, updatable = false)
    private List<MovLljTareaRealizadasEntity> tareas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDate.now();
        if (this.activo == null) this.activo = "1";
        if (this.id != null) {
            beneficiadas.forEach(x -> x.setLljId(this.id));
            atendidas.forEach(x -> x.setLljId(this.id));
            casos.forEach(x -> x.setLljId(this.id));
            tareas.forEach(x -> x.setLljId(this.id));
        }
    }
}