package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;

@Data
@Entity
@Table(name = "mae_aju_institucion_educativas", schema = EsquemaConstants.PRUEBA)
public class MaeInstitucionEducativaEntity implements Serializable {

    @Id
    @Column(name = "c_institucion_id", length = 7)
    private String id;

    @Column(name = "x_nombre", length = 200)
    private String nombre;

    @Column(name = "x_nivel", length = 200)
    private String nivel; // Primaria, Secundaria

    @Column(name = "x_director", length = 200)
    private String director;

    @Column(name = "c_telefono", length = 10)
    private String telefono;

    @Column(name = "x_direccion", length = 200)
    private String direccion;

    @Column(name = "c_ugel_id", length = 11)
    private String ugelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_ugel_id", insertable = false, updatable = false)
    private MaeUgelEntity ugel;
}