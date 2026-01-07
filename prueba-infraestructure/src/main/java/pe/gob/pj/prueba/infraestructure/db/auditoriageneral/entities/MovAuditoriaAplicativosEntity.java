package pe.gob.pj.prueba.infraestructure.db.auditoriageneral.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;


/**
 * 
 * Clase que mapea las propiedades de la tabla MOV_AUDITORIA_APLICATIVOS
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "MOV_AUDITORIA_APLICATIVOS", schema = EsquemaConstants.AUDITORIA_GENERAL)
public class MovAuditoriaAplicativosEntity implements Serializable {

  static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GEN_MOV_AUDITORIA_APLICATIVOS")
  @SequenceGenerator(name = "GEN_MOV_AUDITORIA_APLICATIVOS",
      schema = EsquemaConstants.AUDITORIA_GENERAL,
      sequenceName = "USEQ_MOV_AUDITORIA_APLICATIVOS", initialValue = 1, allocationSize = 1)
  @Column(name = "N_AUDITORIA_APLICATIVO", nullable = false)
  Long id;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "F_REGISTRO", nullable = false)
  Date fechaRegistro = new Date();

  @Column(name = "X_URI", nullable = false)
  String uri;

  @Column(name = "X_PETICION_URL", nullable = true)
  String peticionUrl;

  @Column(name = "X_PETICION_BODY", nullable = true, columnDefinition = "text")
  String peticionBody;

  @Column(name = "C_USUARIO_APLICATIVO", nullable = true)
  String usuarioAplicativo;

  @Column(name = "C_USUARIO_AUTH", nullable = false)
  String usuarioAuth;

  @Column(name = "X_IPS", nullable = false)
  String ips;

  @Column(name = "C_CUO_PETICION", nullable = false)
  String codigoUnicoOperacion;

  @Column(name = "C_RESPUESTA", nullable = false)
  String codigoRespuesta;

  @Column(name = "X_RESPUESTA", nullable = false)
  String descripcionRespuesta;

  @Column(name = "N_DURACION_RESPUESTA", nullable = false)
  Integer duracionRespuesta;

  @Column(name = "X_HERRAMIENTA_CONSUMO", nullable = true)
  String herramientaConsume;

  // Auditoria
  @Column(name = "F_AUD")
  LocalDateTime fAud = LocalDateTime.now();
  @Column(name = "B_AUD")
  private String bAud = OperacionBaseDatos.INSERTAR.getNombre();
  @Column(name = "C_AUD_UID")
  private String cAudId;
  @Column(name = "C_AUD_UIDRED")
  private String cAudIdRed = InformacionRedUtils.getNombreRed();
  @Column(name = "C_AUD_PC")
  private String cAudPc = InformacionRedUtils.getPc();
  @Column(name = "C_AUD_IP")
  private String cAudIp = InformacionRedUtils.getIp();
  @Column(name = "C_AUD_MCADDR")
  private String cAudMcAddr = InformacionRedUtils.getMac();
  @Column(name = "L_ACTIVO", length = 1, nullable = false)
  private String activo = Estado.ACTIVO_NUMERICO.getNombre();

}
