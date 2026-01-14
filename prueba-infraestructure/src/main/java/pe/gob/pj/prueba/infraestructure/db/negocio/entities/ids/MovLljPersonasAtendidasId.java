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
public class MovLljPersonasAtendidasId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "c_llj_id")
    private String lljId;

    @Column(name = "n_tipo_vuln_id")
    private Integer tipoVulnerabilidadId;

    // ✅ CORREGIDO: En tu BD es 'c_rango', no 'c_rango_edad'
    @Convert(converter = TrimStringConverter.class)
    @Column(name = "c_rango")
    private String rangoEdad;
}