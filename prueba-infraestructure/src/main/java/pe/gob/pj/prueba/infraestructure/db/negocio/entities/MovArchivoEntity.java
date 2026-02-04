package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;
import pe.gob.pj.prueba.domain.common.enums.Estado;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mov_archivo", schema = EsquemaConstants.PRUEBA)
public class MovArchivoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SEQ_MOV_ARCHIVO", schema = EsquemaConstants.PRUEBA, sequenceName = "useq_mov_archivo", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOV_ARCHIVO")
    @Column(name = "n_archivo_id")
    private Long id;

    @Column(name = "x_nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "x_tipo", length = 25, nullable = false)
    private String tipo;

    @Column(name = "x_ruta", length = 150, nullable = false)
    private String ruta;

    @Column(name = "c_num_identif", length = 17, nullable = false)
    private String numeroIdentificacion;

    // --- AUDITORÍA ---

    @Column(name = "l_activo", length = 1, nullable = false)
    private String activo;

    @Column(name = "f_registro", insertable = false, updatable = false)
    private LocalDateTime fRegistro;

    @Column(name = "f_aud")
    private LocalDateTime fAud;

    @Column(name = "b_aud")
    private String bAud;

    @Column(name = "c_aud_uid")
    private String cAudId;

    @Column(name = "c_aud_uidred")
    private String cAudIdRed;

    @Column(name = "c_aud_pc")
    private String cAudPc;

    @Column(name = "c_aud_ip")
    private String cAudIp;

    @Column(name = "c_aud_mcaddr")
    private String cAudMcAddr;

    @PrePersist
    public void prePersist() {
        this.fAud = LocalDateTime.now();
        this.bAud = OperacionBaseDatos.INSERTAR.getNombre();
        this.activo = Estado.ACTIVO_NUMERICO.getNombre();

        // Si no se setearon desde el dominio, usar defaults de red
        if (this.cAudIdRed == null) this.cAudIdRed = InformacionRedUtils.getNombreRed();
        if (this.cAudPc == null) this.cAudPc = InformacionRedUtils.getPc();
        if (this.cAudIp == null) this.cAudIp = InformacionRedUtils.getIp();
        if (this.cAudMcAddr == null) this.cAudMcAddr = InformacionRedUtils.getMac();
    }

    @PreUpdate
    public void preUpdate() {
        this.fAud = LocalDateTime.now();
        this.bAud = OperacionBaseDatos.ACTUALIZAR.getNombre();
    }
}