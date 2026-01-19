package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTipoUsuarioEntity;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mae_aju_usuarios", schema = EsquemaConstants.PRUEBA)
public class MaeAjuUsuarioEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_usuario_id", length = 25, nullable = false)
    private String id; // Login / Username

    @Column(name = "x_nombre_completo", length = 50, nullable = false)
    private String nombreCompleto;

    @Column(name = "x_cargo", length = 100, nullable = false)
    private String cargo;

    @Column(name = "c_sigla", length = 3, nullable = false)
    private String sigla;

    @Column(name = "x_email", length = 50, nullable = false)
    private String email;

    @Column(name = "x_password", length = 100, nullable = false)
    private String password;

    @Column(name = "l_activo", length = 1, nullable = false)
    private String activo;

    @Column(name = "x_ruta_foto", length = 150, nullable = false)
    private String rutaFoto;

    @Column(name = "x_nom_foto", length = 8, nullable = false)
    private String nombreFoto;

    // --- RELACIONES (Foreign Keys) ---

    // 1. Relación con Tipo de Usuario (Creada arriba)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_tipo_usu_id", nullable = false)
    private MaeTipoUsuarioEntity tipoUsuario;

    // 2. Relación con Distrito Judicial
    // (Mapeada como columna simple String por ahora para que compile sin la entidad de Distrito)
    @Column(name = "c_distrito_jud_id", length = 2, nullable = false)
    private String distritoJudicialId;

    // 3. Relación con Instancia
    // (Mapeada como columna simple String por ahora)
    @Column(name = "c_instancia_id", length = 3, nullable = false)
    private String instanciaId;
}