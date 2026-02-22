package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
@Table(name = "MAE_INSTANCIA", schema = EsquemaConstants.PRUEBA)
public class MaeInstanciaEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MAE_INSTANCIA", schema = EsquemaConstants.PRUEBA,
            sequenceName = "USEQ_MAE_INSTANCIA", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MAE_INSTANCIA")
    @Column(name = "N_INSTANCIA_ID", nullable = false)
    Long id;

    @Column(name = "X_DESCRIPCION", length = 150, nullable = false)
    String descripcion;

    @Column(name = "X_ARTICULO", length = 5, nullable = false)
    String articulo;

    @Column(name = "N_SEDE_ID", nullable = false)
    Long sedeId;

    // --- CAMPOS DE AUDITORIA Y CONTROL ---

    @Column(name = "L_ACTIVO", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    @Column(name = "F_REGISTRO", insertable = false, updatable = false)
    LocalDateTime fRegistro;

    @Column(name = "F_AUD")
    LocalDateTime fAud = LocalDateTime.now();

    @Column(name = "B_AUD")
    String bAud = OperacionBaseDatos.INSERTAR.getNombre();

    @Column(name = "C_AUD_UID")
    String cAudId;

    @Column(name = "C_AUD_UIDRED")
    String cAudIdRed = InformacionRedUtils.getNombreRed();

    @Column(name = "C_AUD_PC")
    String cAudPc = InformacionRedUtils.getPc();

    @Column(name = "C_AUD_IP")
    String cAudIp = InformacionRedUtils.getIp();

    @Column(name = "C_AUD_MCADDR")
    String cAudMcAddr = InformacionRedUtils.getMac();
}