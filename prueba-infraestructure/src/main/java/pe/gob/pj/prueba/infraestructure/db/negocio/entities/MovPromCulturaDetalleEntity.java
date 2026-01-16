package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovPromCulturaDetalleId; // IMPORTANTE
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mov_aju_actv_prom_culturas_det", schema = EsquemaConstants.PRUEBA)
@IdClass(MovPromCulturaDetalleId.class)
public class MovPromCulturaDetalleEntity implements Serializable {

    @Id
    @Column(name = "c_actv_prom_cult_id", length = 17)
    @Convert(converter = TrimStringConverter.class)
    private String promocionCulturaId;

    @Id
    @Column(name = "c_rango", length = 6)
    @Convert(converter = TrimStringConverter.class)
    private String codigoRango;

    @Id
    @Column(name = "x_desc_rango", length = 35)
    @Convert(converter = TrimStringConverter.class)
    private String descripcionRango;

    @Column(name = "n_cant_fem")
    private Integer cantidadFemenino;

    @Column(name = "n_cant_mas")
    private Integer cantidadMasculino;

    @Column(name = "n_cant_lgtbiq")
    private Integer cantidadLgtbiq;
}