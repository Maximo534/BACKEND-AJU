package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mae_tipo_participante", schema = EsquemaConstants.PRUEBA)
public class MaeTipoParticipanteEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MAE_TIPO_PART", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mae_tipo_participante", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MAE_TIPO_PART")
    @Column(name = "n_tipo_part_id", nullable = false)
    Long id;

    @Column(name = "x_descripcion", length = 100, nullable = false)
    String descripcion;

    @Column(name = "x_detalle", length = 100, nullable = false)
    String detalle;

    // --- AUDITORÍA ---
    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    @Column(name = "f_registro", insertable = false, updatable = false)
    LocalDateTime fRegistro;

    @Column(name = "f_aud")
    LocalDateTime fAud = LocalDateTime.now();

    @Column(name = "b_aud")
    String bAud = OperacionBaseDatos.INSERTAR.getNombre();

    @Column(name = "c_aud_uid")
    String cAudId;

    @Column(name = "c_aud_uidred")
    String cAudIdRed = InformacionRedUtils.getNombreRed();

    @Column(name = "c_aud_pc")
    String cAudPc = InformacionRedUtils.getPc();

    @Column(name = "c_aud_ip")
    String cAudIp = InformacionRedUtils.getIp();

    @Column(name = "c_aud_mcaddr")
    String cAudMcAddr = InformacionRedUtils.getMac();
}