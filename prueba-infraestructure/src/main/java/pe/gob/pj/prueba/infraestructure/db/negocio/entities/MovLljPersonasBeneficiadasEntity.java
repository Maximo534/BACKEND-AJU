package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovLljPersonasBeneficiadasId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;

import java.io.Serializable;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "mov_llj_persona_beneficiada", schema = EsquemaConstants.PRUEBA)
@IdClass(MovLljPersonasBeneficiadasId.class)
public class MovLljPersonasBeneficiadasEntity implements Serializable {

    @Id
    @Column(name = "n_llj_id")
    Long lljId;

    @Id
    @Column(name = "x_desc_rango")
    String descripcionRango;

    @Id
    @Column(name = "c_rango", length = 6)
    @Convert(converter = TrimStringConverter.class)
    String codigoRango;

    @Column(name = "n_cant_fem") Integer cantFemenino;
    @Column(name = "n_cant_mas") Integer cantMasculino;
    @Column(name = "n_cant_lgtbiq") Integer cantLgtbiq;

    @Column(name = "l_activo") String activo = "1";
}