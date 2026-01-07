package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovJiPersonasAtendidasId;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mov_aju_ji_per_atendidas", schema = "public")
@IdClass(MovJiPersonasAtendidasId.class)
public class MovJiPersonasAtendidasEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_just_itin_id", length = 17)
    private String justiciaItineranteId;

    @Id
    @Column(name = "n_tipo_vuln_id")
    private Integer tipoVulnerabilidadId;

    @Id
    @Column(name = "c_rango", length = 5)
    private String rangoEdad;

    @Column(name = "n_cant_fem", nullable = false)
    private Integer cantidadFemenino;

    @Column(name = "n_cant_mas", nullable = false)
    private Integer cantidadMasculino;

    @Column(name = "n_cant_lgtbiq", nullable = false)
    private Integer cantidadLgtbiq;

    @PrePersist
    public void prePersist() {
        if(this.cantidadFemenino == null) this.cantidadFemenino = 0;
        if(this.cantidadMasculino == null) this.cantidadMasculino = 0;
        if(this.cantidadLgtbiq == null) this.cantidadLgtbiq = 0;
    }
}