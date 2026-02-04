package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovLljPersonasAtendidasId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;

import java.io.Serializable;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "mov_llj_per_atendidas", schema = EsquemaConstants.PRUEBA)
@IdClass(MovLljPersonasAtendidasId.class)
public class MovLljPersonasAtendidasEntity implements Serializable {

    @Id
    @Column(name = "n_llj_id")
    Long lljId;

    @Id
    @Column(name = "n_tipo_vuln_id")
    Long tipoVulnerabilidadId;

    @Id
    @Column(name = "c_rango", length = 6)
    @Convert(converter = TrimStringConverter.class)
    String rangoEdad;

    @Column(name = "n_cant_fem") Integer cantidadFemenino;
    @Column(name = "n_cant_mas") Integer cantidadMasculino;
    @Column(name = "n_cant_lgtbiq") Integer cantidadLgtbiq;

    @Column(name = "l_activo") String activo = "1";
}