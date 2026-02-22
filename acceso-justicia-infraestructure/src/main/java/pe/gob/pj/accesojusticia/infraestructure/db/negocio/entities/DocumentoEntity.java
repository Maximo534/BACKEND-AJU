package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.common.enums.Estado;
import pe.gob.pj.accesojusticia.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.accesojusticia.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.accesojusticia.infraestructure.common.utils.InformacionRedUtils;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeCategoriaDocumentoEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mov_documento", schema = EsquemaConstants.PRUEBA)
public class DocumentoEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_DOCUMENTO", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_documento", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_DOCUMENTO")
    @Column(name = "n_documento_id", nullable = false)
    Long id; // PK Long

    @Column(name = "x_nombre", length = 250, nullable = false)
    String nombre;

    @Column(name = "c_tipo", length = 60, nullable = false)
    String tipo;

    @Column(name = "c_formato", length = 5, nullable = false)
    String formato;

    @Column(name = "x_ruta", length = 250, nullable = false)
    String ruta;

    @Column(name = "n_periodo", nullable = false)
    Integer periodo;

    // --- RELACIÓN MAESTRA ---
    @Column(name = "n_categoria_doc_id", nullable = false)
    Long categoriaDocumentoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "n_categoria_doc_id", insertable = false, updatable = false)
    MaeCategoriaDocumentoEntity categoria;

    // --- AUDITORÍA ESTÁNDAR ---
    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    @Column(name = "f_registro", insertable = false, updatable = false)
    LocalDateTime fRegistro;

    @Column(name = "f_aud")
    LocalDateTime fAud = LocalDateTime.now();

    @Column(name = "b_aud")
    String bAud = OperacionBaseDatos.INSERTAR.getNombre();

    @Column(name = "c_aud_uid") String cAudId;
    @Column(name = "c_aud_uidred") String cAudIdRed = InformacionRedUtils.getNombreRed();
    @Column(name = "c_aud_pc") String cAudPc = InformacionRedUtils.getPc();
    @Column(name = "c_aud_ip") String cAudIp = InformacionRedUtils.getIp();
    @Column(name = "c_aud_mcaddr") String cAudMcAddr = InformacionRedUtils.getMac();
}