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
@Table(name = "mov_justicia_itinerante", schema = EsquemaConstants.PRUEBA)
public class MovJusticiaItineranteEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_JI", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_justicia_itinerante", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_JI")
    @Column(name = "n_just_itin_id", nullable = false)
    Long id;

    // --- DATOS PRINCIPALES ---
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
    @Column(name = "n_eje_id")
    Long ejeId;
    @Column(name = "x_publico_obj", length = 150)
    String publicoObjetivo;
    @Column(name = "x_publico_obj_det", length = 50)
    String publicoObjetivoDetalle;
    @Column(name = "x_lugar_activ", length = 150)
    String lugarActividad;

    // UBIGEO
    @Column(name = "n_departamento_id") Long departamentoId;
    @Column(name = "n_provincia_id") Long provinciaId;
    @Column(name = "n_distrito_id") Long distritoId;

    // ESTADÍSTICAS
    @Column(name = "n_num_mesas_inst") Integer numMesasInstaladas;
    @Column(name = "n_num_ser_bri_ate") Integer numServidores;
    @Column(name = "n_num_juez_ate") Integer numJueces;
    @Column(name = "l_adc_pueb_indg", length = 2) String codigoAdcPueblosIndigenas;
    @Column(name = "x_tambo", length = 100) String tambo;
    @Column(name = "l_sae_leng_nativa", length = 2) String codigoSaeLenguaNativa;
    @Column(name = "x_leng_nativa", length = 25) String lenguaNativa;

    // TEXTOS
    @Column(name = "t_des_activ_realz", columnDefinition = "TEXT") String descripcionActividad;
    @Column(name = "t_inst_aliada", columnDefinition = "TEXT") String institucionesAliadas;
    @Column(name = "t_observacion", columnDefinition = "TEXT") String observaciones;

    // OTROS
    @Column(name = "f_reg_activ") LocalDate fechaRegistroActividad = LocalDate.now();
    @Column(name = "n_usuario_reg_id") Long usuarioRegistroId;
    @Column(name = "l_activo", length = 1, nullable = false) String activo = Estado.ACTIVO_NUMERICO.getNombre();

    // --- AUDITORÍA PADRE ---
    @Column(name = "f_registro", insertable = false, updatable = false) LocalDateTime fRegistro;
    @Column(name = "f_aud") LocalDateTime fAud = LocalDateTime.now();
    @Column(name = "b_aud") String bAud = OperacionBaseDatos.INSERTAR.getNombre();
    @Column(name = "c_aud_uid") String cAudId;
    @Column(name = "c_aud_uidred") String cAudIdRed = InformacionRedUtils.getNombreRed();
    @Column(name = "c_aud_pc") String cAudPc = InformacionRedUtils.getPc();
    @Column(name = "c_aud_ip") String cAudIp = InformacionRedUtils.getIp();
    @Column(name = "c_aud_mcaddr") String cAudMcAddr = InformacionRedUtils.getMac();

    // --- RELACIONES HIJAS (Bidireccionales con mappedBy) ---

    @OneToMany(mappedBy = "justiciaItinerante", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovJiPersonasAtendidasEntity> personasAtendidas = new ArrayList<>();

    @OneToMany(mappedBy = "justiciaItinerante", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovJiCasosAtendidosEntity> casosAtendidos = new ArrayList<>();

    @OneToMany(mappedBy = "justiciaItinerante", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovJiPersonasBeneficiadasEntity> personasBeneficiadas = new ArrayList<>();

    @OneToMany(mappedBy = "justiciaItinerante", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<MovJiTareasRealizadasEntity> tareasRealizadas = new ArrayList<>();

    // --- LÓGICA DE PERSISTENCIA ---
    @PrePersist
    public void prePersist() {
        // 1. Casos Atendidos
        if (this.casosAtendidos != null) {
            this.casosAtendidos.forEach(hijo -> {
                hijo.setJusticiaItinerante(this);
                hijo.setCAudId(this.cAudId);
            });
        }

        // 2. Personas Atendidas
        if (this.personasAtendidas != null) {
            this.personasAtendidas.forEach(hijo -> {
                hijo.setJusticiaItinerante(this);
                hijo.setCAudId(this.cAudId);
            });
        }

        // 3. Personas Beneficiadas
        if (this.personasBeneficiadas != null) {
            this.personasBeneficiadas.forEach(hijo -> {
                hijo.setJusticiaItinerante(this);
                hijo.setCAudId(this.cAudId);
            });
        }

        // 4. Tareas Realizadas
        if (this.tareasRealizadas != null) {
            this.tareasRealizadas.forEach(hijo -> {
                hijo.setJusticiaItinerante(this);
                hijo.setCAudId(this.cAudId);
            });
        }
    }
}