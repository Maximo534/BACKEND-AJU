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
@Table(name = "mov_actividad_promocion_cultura", schema = EsquemaConstants.PRUEBA)
public class MovPromocionCulturaEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_PROM_CULT", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_actividad_promocion_cultura", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_PROM_CULT")
    @Column(name = "n_actv_prom_cult_id", nullable = false)
    Long id;

    @Column(name = "c_codigo", length = 17, nullable = false, unique = true)
    String codigo;

    @Column(name = "n_distrito_jud_id", nullable = false)
    Long distritoJudicialId;

    @Column(name = "x_nom_autoridad", length = 100)
    String nombreActividad;

    @Column(name = "x_tipo_doc_autoridad", length = 100)
    String tipoActividad;

    @Column(name = "x_dato_autoridad", length = 100)
    String tipoActividadOtros;

    @Column(name = "x_posic_solicitante", length = 150)
    String publicoObjetivo;

    @Column(name = "x_desc_posic_orig", length = 100)
    String publicoObjetivoOtros;

    @Column(name = "l_sub_area_interv", length = 2)
    String seDictoLenguaNativa;

    @Column(name = "x_lengua_nat", length = 25)
    String lenguaNativa;

    @Column(name = "l_cod_prog_presu", length = 2)
    String participaronDiscapacitados;

    @Column(name = "n_cod_prog_proy")
    Integer numeroDiscapacitados;

    @Column(name = "t_recur_utiliz", columnDefinition = "TEXT")
    String institucionesAliadas;

    @Column(name = "l_area_riesgo", length = 2)
    String areaRiesgo;

    @Column(name = "x_zona_intervencion", length = 200)
    String zonaIntervencion;

    @Column(name = "x_modalidad_proy", length = 50)
    String modalidadProyecto;

    @Column(name = "f_inicio")
    LocalDate fechaInicio;

    @Column(name = "f_fin")
    LocalDate fechaFin;

    @Column(name = "x_res_plan_anual", length = 50)
    String resolucionPlanAnual;

    @Column(name = "x_res_admin_plan", length = 50)
    String resolucionAdminPlan;

    @Column(name = "x_doc_autoriza", length = 60)
    String documentoAutoriza;

    @Column(name = "x_lugar_actv", length = 60)
    String lugarActividad;

    // --- Ubigeo ---
    @Column(name = "n_depa_id") Long departamentoId;
    @Column(name = "n_prov_id") Long provinciaId;
    @Column(name = "n_dist_id") Long distritoGeograficoId;

    @Column(name = "n_eje_id")
    Long ejeId;

    @Column(name = "t_desc_activ", columnDefinition = "TEXT")
    String descripcionActividad;

    @Column(name = "t_observacion", columnDefinition = "TEXT")
    String observacion;

    // --- Auditoría ---
    @Column(name = "f_registro")
    LocalDate fechaRegistroActividad = LocalDate.now();

    @Column(name = "n_usuario_reg_id")
    Long usuarioRegistroId;

    @Column(name = "l_activo", length = 1)
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

    // --- Relaciones Corregidas (mappedBy) ---

    @OneToMany(mappedBy = "promocionCultura", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovPromCulturaDetalleEntity> personasBeneficiadas = new ArrayList<>();

    @OneToMany(mappedBy = "promocionCultura", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovPromCulturaTareaEntity> tareas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.personasBeneficiadas != null) {
            this.personasBeneficiadas.forEach(p -> {
                p.setPromocionCultura(this);
                p.setCAudId(this.cAudId);
            });
        }
        if (this.tareas != null) {
            this.tareas.forEach(t -> {
                t.setPromocionCultura(this);
                t.setCAudId(this.cAudId);
            });
        }

        if (this.areaRiesgo == null || this.areaRiesgo.isBlank()) {
            this.areaRiesgo = "00";
        }
    }
}