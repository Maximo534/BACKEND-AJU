package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "mov_aju_actv_prom_culturas", schema = EsquemaConstants.PRUEBA)
public class MovPromocionCulturaEntity implements Serializable {

    @Id
    @Column(name = "c_actv_prom_cult_id", length = 17)
    private String id;

    @Column(name = "c_distrito_jud_id", length = 2, nullable = false)
    private String distritoJudicialId;

    @Column(name = "x_nom_autoridad", length = 100)
    private String nombreActividad;

    @Column(name = "x_tipo_doc_autoridad", length = 100)
    private String tipoActividad;

    @Column(name = "x_dato_autoridad", length = 100)
    private String tipoActividadOtros;

    @Column(name = "x_posic_solicitante", length = 150)
    private String publicoObjetivo;

    @Column(name = "x_desc_posic_orig", length = 100)
    private String publicoObjetivoOtros;

    @Column(name = "l_sub_area_interv", length = 2)
    private String seDictoLenguaNativa;

    @Column(name = "x_lengua_nat", length = 25)
    private String lenguaNativaDesc;

    @Column(name = "l_cod_prog_presu", length = 2)
    private String participaronDiscapacitados;

    @Column(name = "n_cod_prog_proy")
    private Integer numeroDiscapacitados;

    @Column(name = "t_recur_utiliz", columnDefinition = "TEXT")
    private String institucionesAliadas;

    @Column(name = "l_area_riesgo", length = 2)
    private String areaRiesgo;

    @Column(name = "x_zona_intervencion", length = 200)
    private String zonaIntervencion;

    @Column(name = "x_modalidad_proy", length = 50)
    private String modalidadProyecto;

    @Column(name = "f_inicio")
    private LocalDate fechaInicio;

    @Column(name = "f_fin")
    private LocalDate fechaFin;

    @Column(name = "x_res_plan_anual", length = 50)
    private String resolucionPlanAnual;

    @Column(name = "x_res_admin_plan", length = 50)
    private String resolucionAdminPlan;

    @Column(name = "x_doc_autoriza", length = 60)
    private String documentoAutoriza;

    @Column(name = "x_lugar_actv", length = 60)
    private String lugarActividad;

    @Column(name = "c_depa_id", length = 2)
    private String departamentoId;

    @Column(name = "c_prov_id", length = 4)
    private String provinciaId;

    @Column(name = "c_dist_id", length = 6)
    private String distritoGeograficoId;

    @Column(name = "c_eje_id", length = 5)
    private String ejeId;

    @Column(name = "t_desc_activ", columnDefinition = "TEXT")
    private String descripcionActividad;

    @Column(name = "t_observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "f_registro")
    private LocalDate fechaRegistro;

    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;

    @Column(name = "l_activo", length = 1)
    private String activo;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_actv_prom_cult_id", referencedColumnName = "c_actv_prom_cult_id", nullable = false, insertable = false, updatable = false)
    private List<MovPromCulturaDetalleEntity> participantes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_actv_prom_cult_id", referencedColumnName = "c_actv_prom_cult_id", nullable = false, insertable = false, updatable = false)
    private List<MovPromCulturaTareaEntity> tareas = new ArrayList<>();

    // ✅ SOLO LÓGICA ESTRUCTURAL, NADA DE NEGOCIO
    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDate.now();
        if (this.activo == null) this.activo = "1";

        // Mantenemos la integridad de IDs hijos
        if (this.id != null) {
            if (this.participantes != null) this.participantes.forEach(p -> p.setPromocionCulturaId(this.id));
            if (this.tareas != null) this.tareas.forEach(t -> t.setPromocionCulturaId(this.id));
        }
    }
}