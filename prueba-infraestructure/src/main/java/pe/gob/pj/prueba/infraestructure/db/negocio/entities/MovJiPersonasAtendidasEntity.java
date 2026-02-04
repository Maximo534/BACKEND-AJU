package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovJiPersonasAtendidasId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mov_ji_persona_atendida", schema = EsquemaConstants.PRUEBA)
@IdClass(MovJiPersonasAtendidasId.class)
public class MovJiPersonasAtendidasEntity implements Serializable {

    static final long serialVersionUID = 1L;

    // --- CLAVE COMPUESTA ---
    @Id
    @Column(name = "n_just_itin_id")
    Long justiciaItineranteId;

    @Id
    @Column(name = "n_tipo_vuln_id")
    Long tipoVulnerabilidadId;

    @Id
    @Column(name = "c_rango", length = 5)
    @Convert(converter = TrimStringConverter.class)
    String rangoEdad;

    // --- DATOS ---
    @Column(name = "n_cant_fem", nullable = false)
    Integer cantFemenino = 0;

    @Column(name = "n_cant_mas", nullable = false)
    Integer cantMasculino = 0;

    @Column(name = "n_cant_lgtbiq", nullable = false)
    Integer cantLgtbiq = 0;

    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("justiciaItineranteId")
    @JoinColumn(name = "n_just_itin_id")
    private MovJusticiaItineranteEntity justiciaItinerante;

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