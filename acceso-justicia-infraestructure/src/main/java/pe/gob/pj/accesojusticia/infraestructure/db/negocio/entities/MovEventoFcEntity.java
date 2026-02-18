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
@Table(name = "mov_evento", schema = EsquemaConstants.PRUEBA)
public class MovEventoFcEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_EVENTO", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_evento", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_EVENTO")
    @Column(name = "n_evento_id", nullable = false)
    Long id;

    // --- CÓDIGO VISIBLE ---
    @Column(name = "c_codigo", length = 17, nullable = false, unique = true)
    String codigo;

    // --- DATOS NEGOCIO ---
    @Column(name = "n_distrito_jud_id", nullable = false)
    Long distritoJudicialId;

    @Column(name = "c_tipo_evento", length = 25, nullable = false)
    String tipoEvento;

    @Column(name = "x_nombre_evento", length = 150, nullable = false)
    String nombreEvento;

    @Column(name = "f_inicio", nullable = false)
    LocalDate fechaInicio;

    @Column(name = "f_fin", nullable = false)
    LocalDate fechaFin;

    @Column(name = "x_res_plan_anual", length = 50) String resolucionPlanAnual;
    @Column(name = "x_res_admin_plan", length = 50) String resolucionAdminPlan;
    @Column(name = "x_doc_autoriza", length = 60) String documentoAutoriza;

    @Column(name = "n_eje_id", nullable = false)
    Long ejeId;

    @Column(name = "x_modalidad", length = 25, nullable = false)
    String modalidad;

    @Column(name = "n_duracion", nullable = false) Integer duracionHoras;
    @Column(name = "n_sesion", nullable = false) Integer numeroSesiones;
    @Column(name = "x_docente", length = 200, nullable = false) String docenteExpositor;

    @Column(name = "l_interprete", length = 2) String interpreteSenias;
    @Column(name = "n_discapacidad", nullable = false) Integer numeroDiscapacitados;
    @Column(name = "l_lengua_nat", length = 2) String seDictoLenguaNativa;
    @Column(name = "x_lengua_nat_desc", length = 30) String lenguaNativaDesc;

    @Column(name = "x_publico_obj", length = 200, nullable = false) String publicoObjetivo;
    @Column(name = "x_publico_obj_det", length = 50) String publicoObjetivoDetalle;
    @Column(name = "x_nombre_inst", length = 100, nullable = false) String nombreInstitucion;

    // --- UBIGEO  ---
    @Column(name = "n_departamento_id", nullable = false) Long departamentoId;
    @Column(name = "n_provincia_id", nullable = false) Long provinciaId;
    @Column(name = "n_distrito_id", nullable = false) Long distritoId;

    @Column(name = "x_desc_activ", length = 500) String descripcionActividad;
    @Column(name = "x_inst_aliada", length = 500) String institucionesAliadas;
    @Column(name = "x_observacion", length = 500) String observaciones;

    // --- AUDITORÍA ---
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

    // --- RELACIONES CORREGIDAS (mappedBy) ---

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovEventoDetalleEntity> participantes = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovEventoTareaEntity> tareasRealizadas = new ArrayList<>();

    // --- PRE-PERSIST: VINCULAR PADRE E HIJOS ---
    @PrePersist
    public void prePersist() {
        if (this.participantes != null) {
            this.participantes.forEach(p -> {
                p.setEvento(this);
                p.setCAudId(this.cAudId);
            });
        }
        if (this.tareasRealizadas != null) {
            this.tareasRealizadas.forEach(t -> {
                t.setEvento(this);
                t.setCAudId(this.cAudId);
            });
        }
    }
}