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

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mov_jpe_caso_atendido", schema = EsquemaConstants.PRUEBA)
public class MovJpeCasoAtendidoEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_JPE_CASO", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_jpe_caso_atendido", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_JPE_CASO")
    @Column(name = "n_jpeca_id", nullable = false)
    Long id;

    @Column(name = "c_codigo", length = 17, nullable = false, unique = true)
    String codigo;

    @Column(name = "n_distrito_jud_id", nullable = false)
    Long distritoJudicialId;

    @Column(name = "f_registro_caso", nullable = false)
    LocalDate fechaRegistroCaso;

    @Column(name = "x_lugar_activ", length = 150, nullable = false)
    String lugarActividad;

    // --- UBIGEO ---
    @Column(name = "n_depa_id", nullable = false) Long departamentoId;
    @Column(name = "n_prov_id", nullable = false) Long provinciaId;
    @Column(name = "n_dist_id", nullable = false) Long distritoId;

    // --- JUEZ DE PAZ ESCOLAR ---
    @Column(name = "n_juez_paz_id", nullable = false)
    Long juezEscolarId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "n_juez_paz_id", insertable = false, updatable = false)
    MaeJuezPazEscolarEntity juezEscolar;

    // --- ESTUDIANTE 1 ---
    @Column(name = "x_nom_comp_estud_1", length = 80, nullable = false) String nombreEstudiante1;
    @Column(name = "x_dni_estud_1", length = 8, nullable = false) String dniEstudiante1;
    @Column(name = "x_grado_estud_1", length = 1, nullable = false) String gradoEstudiante1;
    @Column(name = "x_secc_estud_1", length = 1, nullable = false) String seccionEstudiante1;

    // --- ESTUDIANTE 2 ---
    @Column(name = "x_nom_comp_estud_2", length = 80, nullable = false) String nombreEstudiante2;
    @Column(name = "x_dni_estud_2", length = 8, nullable = false) String dniEstudiante2;
    @Column(name = "x_grado_estud_2", length = 1, nullable = false) String gradoEstudiante2;
    @Column(name = "x_secc_estud_2", length = 1, nullable = false) String seccionEstudiante2;

    // --- DETALLE DEL CONFLICTO ---
    @Column(name = "x_res_hecho", columnDefinition = "TEXT", nullable = false)
    String resumenHechos;

    @Column(name = "x_acuerdo", columnDefinition = "TEXT", nullable = false)
    String acuerdos;

    // --- AUDITORÍA ESTÁNDAR ---
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