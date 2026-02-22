package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities;

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
@Table(name = "MOV_OPCION_PERFIL", schema = EsquemaConstants.PRUEBA)
public class MovOpcionPerfilEntity implements Serializable {

  static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "SEQ_MOV_OPCION_PERFIL", schema = EsquemaConstants.PRUEBA,
          sequenceName = "USEQ_MOV_OPCION_PERFIL", initialValue = 1, allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_OPCION_PERFIL")
  @Column(name = "N_OPCION_PERFIL_ID", nullable = false)
  Integer id;

  @ManyToOne(optional = false, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "N_OPCION_ID")
  MaeOpcionEntity opcion;

  @ManyToOne(optional = false, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "N_PERFIL_ID")
  MaePerfilEntity perfil;

  // Auditoria
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
  @Column(name = "L_ACTIVO", length = 1, nullable = false)
  String activo = Estado.ACTIVO_NUMERICO.getNombre();
}