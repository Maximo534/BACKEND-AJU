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
@Table(name = "mae_provincia", schema = EsquemaConstants.PRUEBA)
public class MaeProvinciaEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MAE_PROV", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mae_provincia", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MAE_PROV")
    @Column(name = "n_prov_id", nullable = false)
    Long id;

    @Column(name = "c_codigo", length = 4, nullable = false)
    String codigo;

    @Column(name = "x_nombre", length = 45, nullable = false)
    String nombre;

    @Column(name = "x_lati_geog", columnDefinition = "TEXT")
    String latitud;

    @Column(name = "x_long_geog", columnDefinition = "TEXT")
    String longitud;

    @Column(name = "n_dep_id", nullable = false)
    Long departamentoId;

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