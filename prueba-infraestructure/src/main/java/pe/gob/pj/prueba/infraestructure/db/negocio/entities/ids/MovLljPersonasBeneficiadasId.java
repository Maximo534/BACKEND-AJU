package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovLljPersonasBeneficiadasId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "c_llj_id")
    private String lljId;

    // ✅ CORREGIDO: En tu BD es 'c_rango', no 'c_codigo_rango'
    @Convert(converter = TrimStringConverter.class)
    @Column(name = "c_rango")
    private String codigoRango;

    // ✅ CORREGIDO: En tu BD es 'x_desc_rango' y es parte de la PK
    @Convert(converter = TrimStringConverter.class)
    @Column(name = "x_desc_rango")
    private String descripcionRango;
}