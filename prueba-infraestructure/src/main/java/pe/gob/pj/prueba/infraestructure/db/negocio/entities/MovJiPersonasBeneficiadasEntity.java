package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovJiPersonasBeneficiadasId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mov_aju_ji_per_beneficiadas", schema = "public")
@IdClass(MovJiPersonasBeneficiadasId.class)
public class MovJiPersonasBeneficiadasEntity implements Serializable {

    @Id
    @Column(name = "c_just_itin_id", length = 17)
    @Convert(converter = TrimStringConverter.class)
    private String justiciaItineranteId;

    @Id
    @Column(name = "x_desc_rango", length = 30)
    @Convert(converter = TrimStringConverter.class)
    private String descripcionRango;

    @Id
    @Column(name = "c_rango", length = 5)
    @Convert(converter = TrimStringConverter.class)
    private String codigoRango;

    @Column(name = "n_cant_fem", nullable = false)
    private Integer cantFemenino;

    @Column(name = "n_cant_mas", nullable = false)
    private Integer cantMasculino;

    @Column(name = "n_cant_lgtbiq", nullable = false)
    private Integer cantLgtbiq;

    @PrePersist
    public void prePersist() {
        if(this.cantFemenino == null) this.cantFemenino = 0;
        if(this.cantMasculino == null) this.cantMasculino = 0;
        if(this.cantLgtbiq == null) this.cantLgtbiq = 0;
    }
}