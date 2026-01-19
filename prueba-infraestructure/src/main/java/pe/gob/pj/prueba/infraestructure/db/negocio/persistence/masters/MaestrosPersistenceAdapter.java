package pe.gob.pj.prueba.infraestructure.db.negocio.persistence.masters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.pj.prueba.domain.model.negocio.masters.*;
import pe.gob.pj.prueba.domain.port.persistence.negocio.masters.MaestrosPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MaestrosPersistenceAdapter implements MaestrosPersistencePort {

    private final MaeActividadOperativaRepository repoActividad;
    private final MaeIndicadorRepository repoIndicador;
    private final MaeTareaRepository repoTarea;
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeMateriaRepository repoMateria;
    private final MaeTipoVulnerabilidadRepository repoVuln;
    private final MaeTamboRepository repoTambo;
    private final MaePlanAnualRepository repoPlan;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;
    private final MaeTipoParticipanteRepository repoTipoPart;

    @Override
    public List<ActividadOperativa> listarActividades() {
        return repoActividad.findByActivo("1").stream()
                .map(e -> ActividadOperativa.builder().id(e.getId()).descripcion(e.getDescripcion()).activo(e.getActivo()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Indicador> listarIndicadoresPorActividad(String idActividad) {
        return repoIndicador.findByActividadIdAndActivo(idActividad, "1").stream()
                .map(e -> Indicador.builder().id(e.getId()).descripcion(e.getDescripcion()).actividadId(e.getActividad().getId()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Tarea> listarTareasPorIndicador(String idIndicador) {
        return repoTarea.findByIndicadorIdAndActivo(idIndicador, "1").stream()
                .map(e -> Tarea.builder().id(e.getId()).descripcion(e.getDescripcion()).medida(e.getMedida()).tipoDato(e.getTipoDato()).indicadorId(e.getIndicador().getId()).build())
                .collect(Collectors.toList());
    }


    @Override
    public List<DistritoJudicial> listarDistritosJudiciales() {
        return repoDistritoJud.findAll().stream()
                .map(e -> DistritoJudicial.builder().id(e.getId()).descripcion(e.getNombre()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Eje> listarEjes() {
        return repoEje.findByActivo("1").stream()
                .map(e -> Eje.builder().id(e.getId()).descripcion(e.getDescripcion()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Materia> listarMaterias() {
        return repoMateria.findByActivo("1").stream()
                .map(e -> Materia.builder().id(e.getId()).descripcion(e.getDescripcion()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TipoVulnerabilidad> listarTiposVulnerabilidad() {
        return repoVuln.findByActivo("1").stream()
                .map(e -> TipoVulnerabilidad.builder().id(e.getId()).descripcion(e.getDescripcion()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Tambo> listarTambos(String idCorte) {
        return repoTambo.findByDistritoJudicialIdAndActivo(idCorte, "1").stream()
                .map(e -> Tambo.builder().id(e.getId()).nombre(e.getNombre()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Plan> buscarPlanes(String idCorte, String periodo) {
        return repoPlan.findByDistritoJudicialIdAndPeriodo(idCorte, periodo).stream()
                .map(e -> Plan.builder()
                        .id(e.getId())
                        .descripcion(e.getDescripcion())
                        .periodo(e.getPeriodo())
                        .resolucionGerencia(e.getResolucionGerencia())
                        .resolucionAprobacion(e.getResolucionAprobacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Ubigeo> listarDepartamentos() {
        return repoDepa.findAll().stream()
                .map(e -> Ubigeo.builder().id(e.getId()).nombre(e.getNombre()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Ubigeo> listarProvincias(String idDepartamento) {
        return repoProv.findByDepartamentoId(idDepartamento).stream()
                .map(e -> Ubigeo.builder().id(e.getId()).nombre(e.getNombre()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Ubigeo> listarDistritos(String idProvincia) {
        return repoDist.findByProvinciaId(idProvincia).stream()
                .map(e -> Ubigeo.builder().id(e.getId()).nombre(e.getNombre()).build())
                .collect(Collectors.toList());
    }


    @Override
    public List<TipoParticipante> listarTiposParticipantes() {
        return repoTipoPart.findByActivo("1").stream()
                .map(e -> TipoParticipante.builder()
                        .id(e.getId())
                        .descripcion(e.getDescripcion())
                        .detalle(e.getDetalle())
                        .build())
                .collect(Collectors.toList());
    }
}