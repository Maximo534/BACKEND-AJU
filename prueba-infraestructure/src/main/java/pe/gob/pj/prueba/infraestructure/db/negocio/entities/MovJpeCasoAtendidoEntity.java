package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "mov_aju_jpe_caso_atendidos", schema = EsquemaConstants.PRUEBA)
public class MovJpeCasoAtendidoEntity implements Serializable {

    @Id
    @Column(name = "c_jpeca_id", length = 17)
    private String id;

    @Column(name = "c_distrito_jud_id", length = 2)
    private String distritoJudicialId;

    @Column(name = "f_registro")
    private LocalDate fechaRegistro;

    @Column(name = "x_lugar_activ", length = 150)
    private String lugarActividad;

    // --- UBIGEO ---
    @Column(name = "c_depa_id", length = 2)
    private String departamentoId;
    @Column(name = "c_prov_id", length = 4)
    private String provinciaId;
    @Column(name = "c_dist_id", length = 6)
    private String distritoId;

    // =========================================================================
    // ✅ CORRECCIÓN AQUÍ: RELACIÓN CON JUEZ ESCOLAR
    // =========================================================================

    // 1. Campo ID (Solo lectura, para consultas rápidas o mappers simples)
    // Se pone insertable=false, updatable=false porque el dueño de la relación será el objeto de abajo
    @Column(name = "c_cod_reg", length = 36, insertable = false, updatable = false)
    private String juezEscolarId;

    // 2. Objeto Relacional (Este es el que usa el PersistenceAdapter con .setJuezEscolar)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_cod_reg") // Esta columna es la FK real
    private MaeJuezPazEscolarEntity juezEscolar;

    // =========================================================================

    // --- ESTUDIANTE 1 (Quien reporta o víctima) ---
    @Column(name = "x_nom_comp_estud_1", length = 80)
    private String nombreEstudiante1;
    @Column(name = "x_dni_estud_1", length = 8)
    private String dniEstudiante1;
    @Column(name = "x_grado_estud_1", length = 1)
    private String gradoEstudiante1;
    @Column(name = "x_secc_estud_1", length = 1)
    private String seccionEstudiante1;

    // --- ESTUDIANTE 2 (La otra parte) ---
    @Column(name = "x_nom_comp_estud_2", length = 80)
    private String nombreEstudiante2;
    @Column(name = "x_dni_estud_2", length = 8)
    private String dniEstudiante2;
    @Column(name = "x_grado_estud_2", length = 1)
    private String gradoEstudiante2;
    @Column(name = "x_secc_estud_2", length = 1)
    private String seccionEstudiante2;

    // --- DETALLE DEL CONFLICTO ---
    @Column(name = "x_res_hecho", columnDefinition = "TEXT")
    private String resumenHechos;

    @Column(name = "x_acuerdo", columnDefinition = "TEXT")
    private String acuerdos;

    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;

}