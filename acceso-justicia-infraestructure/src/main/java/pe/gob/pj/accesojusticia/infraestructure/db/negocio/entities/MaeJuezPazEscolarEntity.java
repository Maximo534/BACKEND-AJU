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
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeInstitucionEducativaEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mae_juez_paz_escolar", schema = EsquemaConstants.PRUEBA)
public class MaeJuezPazEscolarEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MAE_JUEZ_PAZ_ESC", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mae_juez_paz_escolar", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MAE_JUEZ_PAZ_ESC")
    @Column(name = "n_juez_paz_id", nullable = false)
    Long id;

    @Column(name = "c_cod_reg", length = 36, nullable = false, unique = true)
    String codigo;

    // --- Datos Personales ---
    @Column(name = "c_dni", length = 8, nullable = false) String dni;
    @Column(name = "x_ape_paterno", length = 40, nullable = false) String apePaterno;
    @Column(name = "x_ape_materno", length = 40, nullable = false) String apeMaterno;
    @Column(name = "x_nombre", length = 80, nullable = false) String nombres;
    @Column(name = "f_nacimiento", nullable = false) LocalDate fechaNacimiento;
    @Column(name = "x_genero", length = 15, nullable = false) String genero;

    // --- Datos Escolares ---
    @Column(name = "c_grado", length = 1, nullable = false) String grado;
    @Column(name = "c_seccion", length = 3, nullable = false) String seccion;

    // --- Relaciones Maestras ---
    @Column(name = "n_institucion_id", nullable = false)
    Long institucionEducativaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "n_institucion_id", insertable = false, updatable = false)
    MaeInstitucionEducativaEntity institucionEducativa;

    // --- Contacto ---
    @Column(name = "x_email", length = 100, nullable = false) String email;
    @Column(name = "c_celular", length = 9, nullable = false) String celular;

    // --- Cargo ---
    @Column(name = "x_cargo", length = 50, nullable = false) String cargo;
    @Column(name = "f_juramentacion", nullable = false) LocalDate fechaJuramentacion;
    @Column(name = "x_res_acreditacion", length = 25, nullable = false) String resolucionAcreditacion;

    // --- Auditoría Estándar ---
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