package pe.gob.pj.accesojusticia.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pe.gob.pj.accesojusticia.domain.model.Auditoria;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JusticiaItinerante extends Auditoria implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- IDENTIFICADORES ---
    private Long id;
    private String codigo;

    // --- DATOS GENERALES ---
    private Long distritoJudicialId;
    private String distritoJudicialNombre;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;

    private Long ejeId;
    private String publicoObjetivo;
    private String publicoObjetivoDetalle;
    private String lugarActividad;

    // --- UBIGEO---
    private Long departamentoId;
    private Long provinciaId;
    private Long distritoId;

    // --- ESTADÍSTICAS ---
    private Integer numMesasInstaladas;
    private Integer numServidores;
    private Integer numJueces;

    private String codigoAdcPueblosIndigenas;
    private String tambo;
    private String codigoSaeLenguaNativa;
    private String lenguaNativa;

    // --- TEXTOS ---
    private String descripcionActividad;
    private String institucionesAliadas;
    private String observaciones;

    // --- AUDITORÍA DE NEGOCIO ---
    private LocalDate fechaRegistroActividad;
    private Long usuarioRegistroId;
    private String activo;

    private Integer totalBeneficiadosFem;
    private Integer totalBeneficiadosMas;
    private Integer totalBeneficiadosLgtbi;

    // 2. HIJA: PERSONAS ATENDIDAS (NUEVO)
    // Se agregan para mostrar el total de atendidos desglosado por género
    private Integer totalAtendidosFem;
    private Integer totalAtendidosMas;
    private Integer totalAtendidosLgtbi;

    // 3. HIJA: CASOS ATENDIDOS (Ya estaba)
    private Integer totalDemandas;
    private Integer totalAudiencias;
    private Integer totalSentencias;
    private Integer totalProcesos;
    private Integer totalNotificaciones;
    private Integer totalOrientaciones;

    // 4. HIJA: TAREAS REALIZADAS (NUEVO)
    // Como son tareas variadas, mostraremos la cantidad total ejecutada
    private Integer cantidadTareas;


    // --- LISTAS HIJAS (Detalles) ---
    @Builder.Default
    private List<DetalleBeneficiada> personasBeneficiadas = new ArrayList<>();
    @Builder.Default
    private List<DetalleAtendida> personasAtendidas = new ArrayList<>();
    @Builder.Default
    private List<DetalleCaso> casosAtendidos = new ArrayList<>();
    @Builder.Default
    private List<DetalleTarea> tareasRealizadas = new ArrayList<>();


    // --- AUXILIARES ---
    private String search;
    private LocalDate fRegistro;
    private List<Archivo> archivosGuardados;


    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleBeneficiada implements Serializable {
        private String descripcionRango;
        private String codigoRango;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleAtendida implements Serializable {
        private Long tipoVulnerabilidadId;
        private String descripcionVulnerabilidad;
        private String rangoEdad;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleCaso implements Serializable {
        private Long materiaId;
        private String descripcionMateria;
        private Integer numDemandas;
        private Integer numAudiencias;
        private Integer numSentencias;
        private Integer numProcesos;
        private Integer numNotificaciones;
        private Integer numOrientaciones;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTarea implements Serializable {
        private Long tareaId;
        private String descripcionTarea;
        private LocalDate fechaInicio;
        private String descripcion;
    }
}