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
@Table(name = "MOV_USUARIO", schema = EsquemaConstants.PRUEBA)
public class MovUsuarioEntity implements Serializable {

  static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "SEQ_MOV_USUARIO", schema = EsquemaConstants.PRUEBA,
          sequenceName = "USEQ_MOV_USUARIO", initialValue = 1, allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_USUARIO")
  @Column(name = "N_USUARIO_ID", nullable = false)
  Integer id;

  @Column(name = "X_USUARIO", nullable = false)
  String usuario; // Login

  @Column(name = "X_CLAVE", nullable = false)
  String clave;

  @Column(name = "X_CARGO")
  String cargo;

  @Column(name = "C_SIGLA")
  String sigla;

  @Column(name = "X_EMAIL")
  String email;

  @Column(name = "N_DISTRITO_JUD_ID")
  Integer idDistritoJudicial;

  @Column(name = "N_INSTANCIA_ID")
  Integer idInstancia;

  @Column(name = "X_NOMBRE_COMPLETO")
  String nombreCompleto;

  @Column(name = "X_RUTA_FOTO")
  String rutaFoto;

  @Column(name = "X_NOM_FOTO")
  String nomFoto;
  // ------------------------------------

  @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
  private List<MovUsuarioPerfilEntity> perfils = new ArrayList<>();

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

  @PrePersist
  @PreUpdate
  public void prePersist() {
    if (this.rutaFoto == null || this.rutaFoto.isBlank()) {
      this.rutaFoto = "-";
    }
    if (this.nomFoto == null || this.nomFoto.isBlank()) {
      this.nomFoto = "-";
    }
    if (this.activo == null) {
      this.activo = "1";
    }
  }

}