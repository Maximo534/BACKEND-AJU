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
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.ids.MovJiTareasRealizadasId;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeTareaEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mov_ji_tarea_realizada", schema = EsquemaConstants.PRUEBA)
@IdClass(MovJiTareasRealizadasId.class)
public class MovJiTareasRealizadasEntity implements Serializable {

    static final long serialVersionUID = 1L;

    // --- CLAVE COMPUESTA ---
    @Id
    @Column(name = "n_just_itin_id")
    Long justiciaItineranteId;

    @Id
    @Column(name = "n_tarea_id")
    Long tareaId;

    // --- DATOS ---
    @Column(name = "f_inicio")
    LocalDate fechaInicio;

    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    // --- RELACIONES ---

    // 1. RELACIÓN CON PADRE
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("justiciaItineranteId")
    @JoinColumn(name = "n_just_itin_id")
    private MovJusticiaItineranteEntity justiciaItinerante;

    // 2. RELACIÓN CON MAESTRA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "n_tarea_id", insertable = false, updatable = false)
    MaeTareaEntity tareaMaestra;

    // --- AUDITORÍA AUTOMÁTICA ---

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