package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovPromCulturaTareaId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "mov_actividad_prom_cult_tarea", schema = EsquemaConstants.PRUEBA)
@IdClass(MovPromCulturaTareaId.class)
public class MovPromCulturaTareaEntity implements Serializable {

    // --- ID COMPUESTO ---
    @Id
    @Column(name = "n_actv_prom_cult_id")
    Long promocionCulturaId;

    @Id
    @Column(name = "n_tarea_id")
    Long tareaId;

    // --- DATOS ---
    @Column(name = "f_inicio")
    LocalDate fechaInicio;

    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    // --- RELACIONES ---

    // 1. Con el Padre
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promocionCulturaId")
    @JoinColumn(name = "n_actv_prom_cult_id")
    private MovPromocionCulturaEntity promocionCultura;

    // 2. Con la Maestra
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