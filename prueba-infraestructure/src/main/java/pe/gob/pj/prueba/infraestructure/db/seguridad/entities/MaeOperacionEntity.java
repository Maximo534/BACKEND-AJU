package pe.gob.pj.prueba.infraestructure.db.seguridad.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;

/**
 * 
 * Clase que mapea las propiedades de la tabla indicada en el Table
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Entity
@Table(name = "mae_operacion", schema = EsquemaConstants.ESQUEMA_SEGURIDAD)
public class MaeOperacionEntity implements Serializable {

  static final long serialVersionUID = 1L;

  @Id
  @Column(name = "n_operacion")
  Integer nOperacion;

  @Column(name = "x_descripcion")
  String xDescripcion;

  @Column(name = "x_endpoint")
  String xEndpoint;

  @Column(name = "x_operacion")
  String xOperacion;

  // bi-directional many-to-one association to MaeAplicativo
  @ManyToOne
  @JoinColumn(name = "n_aplicativo")
  MaeAplicativoEntity maeAplicativo;

  // bi-directional many-to-one association to MaeRol
  @ManyToOne
  @JoinColumn(name = "n_rol")
  MaeRolEntity maeRol;
  
  @ManyToOne
  @JoinColumn(name="n_tipo_operacion")
  MaeTiposOperacion maeTiposOperacion;

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
