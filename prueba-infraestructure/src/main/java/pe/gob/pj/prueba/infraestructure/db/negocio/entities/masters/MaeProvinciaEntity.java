package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;

@Entity
@Table(name = "mae_aju_provincias", schema = EsquemaConstants.PRUEBA)
@Data
public class MaeProvinciaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_prov_id", length = 4)
    private String id;

    @Column(name = "x_nombre", length = 45, nullable = false)
    private String nombre;

    @Column(name = "x_lati_geog", columnDefinition = "TEXT")
    private String latitud;

    @Column(name = "x_long_geog", columnDefinition = "TEXT")
    private String longitud;

    @Column(name = "c_depa_id", length = 2, nullable = false)
    private String departamentoId;
}