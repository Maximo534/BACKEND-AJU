package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import pe.gob.pj.accesojusticia.domain.common.enums.Estado;
import pe.gob.pj.accesojusticia.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.accesojusticia.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.accesojusticia.infraestructure.common.utils.InformacionRedUtils;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mae_plan_anual", schema = EsquemaConstants.PRUEBA)
public class MaePlanAnualEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MAE_PLAN", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mae_plan_anual", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MAE_PLAN")
    @Column(name = "n_plan_id", nullable = false)
    Long id;

    @Column(name = "x_descripcion", length = 100, nullable = false)
    String descripcion;

    @Column(name = "c_periodo", length = 4, nullable = false)
    String periodo;

    @Column(name = "x_res_gap", length = 50, nullable = false)
    String resolucionGerencia;

    @Column(name = "x_res_ap", length = 40, nullable = false)
    String resolucionAprobacion;

    @Column(name = "x_sigla", length = 80, nullable = false)
    String sigla;

    @Column(name = "n_distrito_jud_id", nullable = false)
    Long distritoJudicialId;

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