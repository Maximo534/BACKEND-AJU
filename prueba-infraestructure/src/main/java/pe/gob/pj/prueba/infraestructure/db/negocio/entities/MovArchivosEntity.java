package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "mov_aju_archivos")
public class MovArchivosEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "x_nombre", length = 100)
    private String nombre;

    @Column(name = "x_tipo", length = 25, nullable = false)
    private String tipo;

    @Column(name = "x_ruta", length = 150, nullable = false)
    private String ruta;

    @Column(name = "c_num_identif", length = 17, nullable = false)
    private String numeroIdentificacion;
}