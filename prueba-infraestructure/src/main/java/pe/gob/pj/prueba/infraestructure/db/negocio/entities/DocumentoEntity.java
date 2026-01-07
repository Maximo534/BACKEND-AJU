package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "mov_aju_documentos")
public class DocumentoEntity implements Serializable {

    @Id
    @Column(name = "c_documento_id", length = 150)
    private String id;

    @Column(name = "x_nombre", length = 250)
    private String nombre;

    @Column(name = "c_tipo", length = 60)
    private String tipo;

    @Column(name = "c_formato", length = 5)
    private String formato;

    @Column(name = "x_ruta", length = 250)
    private String ruta;

    @Column(name = "n_periodo")
    private Integer periodo;

    @Column(name = "l_activo", length = 1)
    private String activo;

    @Column(name = "n_categoria_doc_id")
    private Integer categoriaId;
}