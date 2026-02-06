package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.common.utils.InformacionRedUtils;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovPromCulturaDetalleId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "mov_actividad_prom_cult_detalle", schema = EsquemaConstants.PRUEBA)
@IdClass(MovPromCulturaDetalleId.class)
public class MovPromCulturaDetalleEntity implements Serializable {

    // --- ID COMPUESTO ---
    @Id
    @Column(name = "n_actv_prom_cult_id")
    Long promocionCulturaId;

    @Id
    @Column(name = "c_rango", length = 6)
    @Convert(converter = TrimStringConverter.class)
    String codigoRango;

    @Id
    @Column(name = "x_desc_rango", length = 35)
    @Convert(converter = TrimStringConverter.class)
    String descripcionRango;

    // --- DATOS ---
    @Column(name = "n_cant_fem") Integer cantidadFemenino;
    @Column(name = "n_cant_mas") Integer cantidadMasculino;
    @Column(name = "n_cant_lgtbiq") Integer cantidadLgtbiq;

    @Column(name = "l_activo", length = 1, nullable = false)
    String activo = Estado.ACTIVO_NUMERICO.getNombre();

    // --- RELACIÓN CON PADRE ---
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promocionCulturaId")
    @JoinColumn(name = "n_actv_prom_cult_id")
    private MovPromocionCulturaEntity promocionCultura;

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