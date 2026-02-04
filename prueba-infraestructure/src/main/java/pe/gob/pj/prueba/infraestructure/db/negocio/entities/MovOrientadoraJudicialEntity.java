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
@Table(name = "mov_meta_anual", schema = EsquemaConstants.PRUEBA)
public class MovOrientadoraJudicialEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_ORIENTADORA", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_meta_anual", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_ORIENTADORA")
    @Column(name = "n_meta_anual_id", nullable = false)
    Long id;

    @Column(name = "c_codigo", length = 17, nullable = false, unique = true)
    String codigo;

    @Column(name = "n_distrito_jud_id", nullable = false)
    Long distritoJudicialId;

    @Column(name = "f_reg_activ", nullable = false)
    LocalDate fechaAtencion;

    // --- DATOS USUARIA ---
    @Column(name = "x_nomb_apell", length = 80, nullable = false) String nombreCompleto;
    @Column(name = "c_tipo_doc", length = 25, nullable = false) String tipoDocumento;
    @Column(name = "c_num_doc", length = 18, nullable = false) String numeroDocumento;
    @Column(name = "x_nacionalidad", length = 25, nullable = false) String nacionalidad;
    @Column(name = "n_edad", nullable = false) Integer edad;
    @Column(name = "c_telefono", length = 9, nullable = false) String telefono;
    @Column(name = "x_direccion", length = 150, nullable = false) String direccion;

    // --- UBIGEO ---
    @Column(name = "n_depa_id", nullable = false) Long departamentoId;
    @Column(name = "n_prov_id", nullable = false) Long provinciaId;
    @Column(name = "n_dist_id", nullable = false) Long distritoId;

    // --- DETALLE DEL CASO ---
    @Column(name = "x_tipo_vulne", length = 150, nullable = false) String tipoVulnerabilidad;
    @Column(name = "x_genero", length = 30, nullable = false) String genero;
    @Column(name = "x_lengua_mat", length = 30, nullable = false) String lenguaMaterna;
    @Column(name = "x_desc_caso_ate", length = 150, nullable = false) String tipoCasoAtendido;
    @Column(name = "c_num_exp", length = 26, nullable = false) String numeroExpediente;
    @Column(name = "x_tipo_vio", length = 150, nullable = false) String tipoViolencia;
    @Column(name = "x_derivacion", length = 150, nullable = false) String derivacionInstitucion;

    @Column(name = "t_resena", columnDefinition = "TEXT", nullable = false)
    String resenaCaso;

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