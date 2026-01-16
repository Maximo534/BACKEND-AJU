package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;

@Data
@Entity
@Table(name = "mae_aju_categoria_documentos", schema = EsquemaConstants.PRUEBA)
public class MaeCategoriaDocumentoEntity implements Serializable {

    @Id
    @Column(name = "n_categoria_doc_id")
    private Integer id;

    @Column(name = "x_descripcion", length = 255)
    private String descripcion;

    @Column(name = "l_activo", length = 1)
    private String activo;
}