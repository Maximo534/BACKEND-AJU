package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "mov_aju_eventos", schema = "public")
public class MovEventoFcEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_evento_id", length = 17)
    private String id;
    @Column(name = "c_distrito_jud_id", length = 2, nullable = false)
    private String distritoJudicialId;
    @Column(name = "c_tipo_evento", length = 25)
    private String tipoEvento;
    @Column(name = "x_nombre_evento", length = 150)
    private String nombreEvento;
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
    @Column(name = "c_eje_id", length = 5)
    private String ejeId;
    @Column(name = "x_modalidad", length = 25)
    private String modalidad;
    @Column(name = "n_duracion")
    private Integer duracionHoras;
    @Column(name = "n_sesion")
    private Integer numeroSesiones;
    @Column(name = "x_docente", length = 200)
    private String docenteExpositor;
    @Column(name = "l_interprete", length = 2)
    private String interpreteSenias;
    @Column(name = "n_discapacidad")
    private Integer numeroDiscapacitados;
    @Column(name = "l_lengua_nat", length = 2)
    private String seDictoLenguaNativa;
    @Column(name = "x_lengua_nat_desc", length = 30)
    private String lenguaNativaDesc;
    @Column(name = "x_publico_obj", length = 200)
    private String publicoObjetivo;
    @Column(name = "x_publico_obj_det", length = 50)
    private String publicoObjetivoDetalle;
    @Column(name = "x_nombre_inst", length = 100)
    private String nombreInstitucion;
    @Column(name = "c_depa_id", length = 2)
    private String departamentoId;
    @Column(name = "c_prov_id", length = 4)
    private String provinciaId;
    @Column(name = "c_dist_id", length = 6)
    private String distritoGeograficoId;
    @Column(name = "x_desc_activ", length = 500)
    private String descripcionActividad;
    @Column(name = "x_inst_aliada", length = 500)
    private String institucionesAliadas;
    @Column(name = "x_observacion", length = 500)
    private String observaciones;
    @Column(name = "f_registro")
    private LocalDate fechaRegistro;
    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;
    @Column(name = "l_activo", length = 1)
    private String activo;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_evento_id", referencedColumnName = "c_evento_id", nullable = false, insertable = false, updatable = false)
    private List<MovEventoDetalleEntity> participantes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_evento_id", referencedColumnName = "c_evento_id", nullable = false, insertable = false, updatable = false)
    private List<MovEventoTareaEntity> tareas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDate.now();
        if (this.activo == null) this.activo = "1";
        if (this.id != null) {
            if(this.participantes != null) this.participantes.forEach(p -> p.setEventoId(this.id));
            if(this.tareas != null) this.tareas.forEach(t -> t.setEventoId(this.id));
        }
    }
}