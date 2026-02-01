package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;
// Importamos tu clase ID
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovProgramacionEjeId;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "MOV_PROGRAMACION_EJE", schema = EsquemaConstants.PRUEBA)
@IdClass(MovProgramacionEjeId.class)
public class MovProgramacionEjeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    // --- CAMPOS DE LA PK  ---

    @Id
    @Column(name = "N_DISTRITO_JUD_ID")
    Integer idDistritoJudicial;

    @Id
    @Column(name = "C_PERIODO")
    String periodo;

    @Id
    @Column(name = "N_EJE_ID")
    Integer idEje;

    @Id
    @Column(name = "N_USUARIO_REG_ID")
    Integer idUsuario;

    @Column(name = "L_ACTIVO")
    String activo;

    @Column(name = "F_REGISTRO")
    LocalDateTime fRegistro = LocalDateTime.now();

    // Auditoría
    @Column(name = "F_AUD")
    LocalDateTime fAud = LocalDateTime.now();
    @Column(name = "B_AUD")
    String bAud = "I";
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