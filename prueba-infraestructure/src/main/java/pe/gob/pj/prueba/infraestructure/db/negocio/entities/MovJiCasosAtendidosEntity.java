package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovJiCasosAtendidosId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeMateriaEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "mov_ji_caso_atendido", schema = EsquemaConstants.PRUEBA)
@IdClass(MovJiCasosAtendidosId.class)
public class MovJiCasosAtendidosEntity implements Serializable {

    // --- IDs ---
    @Id @Column(name = "n_just_itin_id") Long justiciaItineranteId;
    @Id @Column(name = "n_materia_id") Long materiaId;

    // --- DATOS ---
    @Column(name = "n_cant_demanda") Integer cantidadDemandas;
    @Column(name = "n_cant_audt") Integer cantidadAudiencias;
    @Column(name = "n_cant_sent") Integer cantidadSentencias;
    @Column(name = "n_cant_proceso") Integer cantidadProcesos;
    @Column(name = "n_cant_notifi") Integer cantidadNotificaciones;
    @Column(name = "n_cant_orienta") Integer cantidadOrientaciones;

    @Column(name = "l_activo") String activo = "1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "n_materia_id", insertable = false, updatable = false)
    private MaeMateriaEntity materia;
    // --- RELACIÓN CON PADRE (OBLIGATORIA para @MapsId) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("justiciaItineranteId")
    @JoinColumn(name = "n_just_itin_id")
    private MovJusticiaItineranteEntity justiciaItinerante;

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