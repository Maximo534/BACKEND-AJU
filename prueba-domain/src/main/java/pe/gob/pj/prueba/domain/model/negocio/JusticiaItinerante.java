package pe.gob.pj.prueba.domain.model.negocio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JusticiaItinerante implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String distritoJudicialId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;

    private String ejeId;
    private String publicoObjetivo;
    private String publicoObjetivoDetalle;
    private String lugarActividad;

    // Ubigeo
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    // --- ESTADÍSTICAS Y DESCRIPCIÓN ---
    // (Nombres actualizados para coincidir con Entity y Request)

    private Integer numMujeresIndigenas;         // Antes: numMesasPartes
    private Integer numPersonasNoIdiomaNacional; // Antes: numServidores
    private Integer numJovenesQuechuaAymara;  // n_jov_quech_aymar
    private String codigoAdcPueblosIndigenas; // l_adc_pueb_indg
    private String tambo;                     // x_tambo
    private String codigoSaeLenguaNativa;     // l_sae_leng_nativa
    private String lenguaNativa;              // x_leng_nativa

    private String descripcionActividad;
    private String institucionesAliadas;
    private String observaciones;

    // Auditoría
    private LocalDate fechaRegistro;
    private String usuarioRegistro;
    private String activo;

    // --- LISTAS DE DETALLE ---

    // Detalle 1: Personas Beneficiadas (Aún pendiente de implementar en BD)
    private List<DetallePersonasBeneficiadas> personasBeneficiadas = new ArrayList<>();

    // Detalle 2: Personas Atendidas
    private List<DetallePersonasAtendidas> personasAtendidas = new ArrayList<>();

    // Detalle 3: Casos Atendidos
    private List<DetalleCasosAtendidos> casosAtendidos = new ArrayList<>();

    // Detalle 4: Tareas Realizadas (Aún pendiente de implementar en BD)
    private List<DetalleTareaRealizada> tareasRealizadas = new ArrayList<>();

    private List<Archivo> archivosGuardados;
}