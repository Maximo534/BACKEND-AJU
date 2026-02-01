package pe.gob.pj.prueba.infraestructure.db.negocio.persistence.masters;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.model.negocio.Perfil;
import pe.gob.pj.prueba.domain.model.negocio.masters.*;
import pe.gob.pj.prueba.domain.port.persistence.negocio.masters.MaestrosPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MaePerfilRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;
import pe.gob.pj.prueba.infraestructure.mappers.PerfilMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaestrosPersistenceAdapter implements MaestrosPersistencePort {

    MaeActividadOperativaRepository repoActividad;
    MaeIndicadorRepository repoIndicador;
    MaeTareaRepository repoTarea;
    MaeDistritoJudicialRepository repoDistritoJud;
    MaeEjeRepository repoEje;
    MaeMateriaRepository repoMateria;
    MaeTipoVulnerabilidadRepository repoVuln;
    MaeTamboRepository repoTambo;
    MaePlanAnualRepository repoPlan;
    MaeDepartamentoRepository repoDepa;
    MaeProvinciaRepository repoProv;
    MaeDistritoRepository repoDist;
    MaeTipoParticipanteRepository repoTipoPart;
    MaePerfilRepository perfilRepository;
    MaeSedeRepository repoSede;
    MaeInstanciaRepository repoInstancia;

    PerfilMapper perfilMapper;

    @Override
    public List<ActividadOperativa> listarActividades(String cuo) {
        return repoActividad.findByActivo("1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Indicador> listarIndicadoresPorActividad(String cuo, Long idActividad) {
        return repoIndicador.findByActividadIdAndActivo(idActividad, "1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> listarTareasPorIndicador(String cuo, Long idIndicador) {
        return repoTarea.findByIndicadorIdAndActivo(idIndicador, "1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<DistritoJudicial> listarDistritosJudiciales(String cuo) {
        return repoDistritoJud.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Sede> listarSedesPorCorte(String cuo, Long idCorte) {
        return repoSede.findByDistritoJudicialIdAndActivo(idCorte, "1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Instancia> listarInstanciasPorSede(String cuo, Long idSede) {
        return repoInstancia.findBySedeIdAndActivo(idSede, "1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Eje> listarEjes(String cuo) {
        return repoEje.findByActivo("1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Materia> listarMaterias(String cuo) {
        return repoMateria.findByActivo("1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<TipoVulnerabilidad> listarTiposVulnerabilidad(String cuo) {
        return repoVuln.findByActivo("1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tambo> listarTambos(String cuo, Long idCorte) {
        return repoTambo.findByDistritoJudicialIdAndActivo(idCorte, "1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Plan> buscarPlanes(String cuo, Long idCorte, String periodo) {
        return repoPlan.findByDistritoJudicialIdAndPeriodo(idCorte, periodo).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Departamento> listarDepartamentos(String cuo) {
        return repoDepa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Provincia> listarProvincias(String cuo, Long idDepartamento) {
        return repoProv.findByDepartamentoId(idDepartamento).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Distrito> listarDistritos(String cuo, Long idProvincia) {
        return repoDist.findByProvinciaId(idProvincia).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<TipoParticipante> listarTiposParticipantes(String cuo) {
        return repoTipoPart.findByActivo("1").stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Perfil> listarPerfilesPermitidos(String cuo, Integer idRolLogueado) {
        return perfilMapper.toDomainList(perfilRepository.listarPerfilesPermitidos(idRolLogueado));
    }

    // ... MÉTODOS PRIVADOS DE MAPEO  ...
    private ActividadOperativa toDomain(MaeActividadOperativaEntity e) {
        return ActividadOperativa.builder().id(e.getId()).descripcion(e.getDescripcion()).activo(e.getActivo()).build();
    }
    private Indicador toDomain(MaeIndicadorEntity e) {
        return Indicador.builder().id(e.getId()).descripcion(e.getDescripcion())
                .actividadId(e.getActividad() != null ? e.getActividad().getId() : null).activo(e.getActivo()).build();
    }
    private Tarea toDomain(MaeTareaEntity e) {
        return Tarea.builder().id(e.getId()).descripcion(e.getDescripcion()).medida(e.getMedida()).tipoDato(e.getTipoDato())
                .indicadorId(e.getIndicador() != null ? e.getIndicador().getId() : null).activo(e.getActivo()).build();
    }
    private DistritoJudicial toDomain(MaeDistritoJudicialEntity e) {
        return DistritoJudicial.builder().id(e.getId()).nombre(e.getNombre()).nombreCorto(e.getNombreCorto()).sigla(e.getSigla()).activo(e.getActivo()).build();
    }
    private Sede toDomain(MaeSedeEntity e) {
        return Sede.builder().id(e.getId()).descripcion(e.getDescripcion()).distritoJudicialId(e.getDistritoJudicialId()).activo(e.getActivo()).build();
    }
    private Instancia toDomain(MaeInstanciaEntity e) {
        return Instancia.builder().id(e.getId()).descripcion(e.getDescripcion()).articulo(e.getArticulo()).sedeId(e.getSedeId()).activo(e.getActivo()).build();
    }
    private Eje toDomain(MaeEjeEntity e) { return Eje.builder().id(e.getId()).descripcion(e.getDescripcion()).activo(e.getActivo()).build(); }
    private Materia toDomain(MaeMateriaEntity e) { return Materia.builder().id(e.getId()).descripcion(e.getDescripcion()).nombreCorto(e.getNombreCorto()).activo(e.getActivo()).build(); }
    private TipoVulnerabilidad toDomain(MaeTipoVulnerabilidadEntity e) { return TipoVulnerabilidad.builder().id(e.getId()).descripcion(e.getDescripcion()).activo(e.getActivo()).build(); }
    private Tambo toDomain(MaeTamboEntity e) { return Tambo.builder().id(e.getId()).nombre(e.getNombre()).distritoJudicialId(e.getDistritoJudicialId()).activo(e.getActivo()).build(); }
    private Plan toDomain(MaePlanAnualEntity e) { return Plan.builder().id(e.getId()).descripcion(e.getDescripcion()).periodo(e.getPeriodo()).resolucionGerencia(e.getResolucionGerencia()).resolucionAprobacion(e.getResolucionAprobacion()).sigla(e.getSigla()).distritoJudicialId(e.getDistritoJudicialId()).activo(e.getActivo()).build(); }
    private Departamento toDomain(MaeDepartamentoEntity e) { return Departamento.builder().id(e.getId()).codigo(e.getCodigo()).nombre(e.getNombre()).distritoJudicialId(e.getDistritoJudicialId()).activo(e.getActivo()).build(); }
    private Provincia toDomain(MaeProvinciaEntity e) { return Provincia.builder().id(e.getId()).codigo(e.getCodigo()).nombre(e.getNombre()).departamentoId(e.getDepartamentoId()).activo(e.getActivo()).build(); }
    private Distrito toDomain(MaeDistritoEntity e) { return Distrito.builder().id(e.getId()).codigo(e.getCodigo()).nombre(e.getNombre()).provinciaId(e.getProvinciaId()).activo(e.getActivo()).build(); }
    private TipoParticipante toDomain(MaeTipoParticipanteEntity e) { return TipoParticipante.builder().id(e.getId()).descripcion(e.getDescripcion()).detalle(e.getDetalle()).activo(e.getActivo()).build(); }
}