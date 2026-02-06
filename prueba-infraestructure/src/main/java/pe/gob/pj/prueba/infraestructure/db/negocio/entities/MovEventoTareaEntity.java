package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovEventoTareaId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "mov_evento_tarea_realizada", schema = EsquemaConstants.PRUEBA)
@IdClass(MovEventoTareaId.class)
public class MovEventoTareaEntity implements Serializable {

    // --- ID COMPUESTO ---
    @Id
    @Column(name = "n_evento_id")
    Long eventoId;

    @Id
    @Column(name = "n_tarea_id")
    Long tareaId;

    // --- DATOS ---
    @Column(name = "f_inicio")
    LocalDate fechaInicio;

    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    // --- RELACIONES ---

    // 1. Con el Padre (Evento)
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventoId")
    @JoinColumn(name = "n_evento_id")
    private MovEventoFcEntity evento;

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

    @Column(name = "c_aud_uidred") String cAudIdRed = InformacionRedUtils.getNombreRed();
    @Column(name = "c_aud_pc") String cAudPc = InformacionRedUtils.getPc();
    @Column(name = "c_aud_ip") String cAudIp = InformacionRedUtils.getIp();
    @Column(name = "c_aud_mcaddr") String cAudMcAddr = InformacionRedUtils.getMac();
}