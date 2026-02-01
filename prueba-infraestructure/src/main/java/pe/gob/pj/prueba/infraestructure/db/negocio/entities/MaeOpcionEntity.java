package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "MAE_OPCION", schema = EsquemaConstants.PRUEBA)
public class MaeOpcionEntity implements Serializable {

  static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "SEQ_MAE_OPCION", schema = EsquemaConstants.PRUEBA,
          sequenceName = "USEQ_MAE_OPCION", initialValue = 1, allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MAE_OPCION")
  @Column(name = "N_OPCION_ID", nullable = false)
  Integer id;

  @Column(name = "C_OPCION", nullable = false)
  String codigo;
  @Column(name = "X_NOMBRE", nullable = false)
  String nombre;
  @Column(name = "X_URL", nullable = false)
  String url;
  @Column(name = "X_ICONO", nullable = false)
  String icono;
  @Column(name = "N_ORDEN", nullable = false)
  Integer orden;

  @ManyToOne(optional = true, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "N_OPCION_SUPERIOR_ID")
  MaeOpcionEntity opcionSuperior;

  @OneToMany(mappedBy = "opcion", fetch = FetchType.LAZY)
  List<MovOpcionPerfilEntity> perfils = new ArrayList<>();

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