package pe.gob.pj.prueba.usecase.negocio.masters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.gob.pj.prueba.domain.model.negocio.masters.*;
import pe.gob.pj.prueba.domain.port.persistence.negocio.masters.MaestrosPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.masters.GestionarMaestrosUseCasePort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionarMaestrosUseCaseAdapter implements GestionarMaestrosUseCasePort {

    private final MaestrosPersistencePort persistencePort;

    @Override
    public List<ActividadOperativa> listarActividadesOperativas() {
        return persistencePort.listarActividades();
    }
    @Override
    public List<Indicador> listarIndicadores(String idActividad) {
        return persistencePort.listarIndicadoresPorActividad(idActividad);
    }
    @Override
    public List<Tarea> listarTareas(String idIndicador) {
        return persistencePort.listarTareasPorIndicador(idIndicador);
    }
    @Override
    public List<DistritoJudicial> listarDistritosJudiciales() {
        return persistencePort.listarDistritosJudiciales();
    }
    @Override
    public List<Eje> listarEjes() {
        return persistencePort.listarEjes();
    }
    @Override
    public List<Materia> listarMaterias() {
        return persistencePort.listarMaterias();
    }
    @Override
    public List<TipoVulnerabilidad> listarTiposVulnerabilidad() {
        return persistencePort.listarTiposVulnerabilidad();
    }
    @Override
    public List<Tambo> listarTambos(String idCorte) {
        return persistencePort.listarTambos(idCorte);
    }
    @Override
    public List<Plan> buscarPlanes(String idCorte, String periodo) {
        return persistencePort.buscarPlanes(idCorte, periodo);
    }
    @Override
    public List<Ubigeo> listarDepartamentos() {
        return persistencePort.listarDepartamentos();
    }
    @Override
    public List<Ubigeo> listarProvincias(String idDepartamento) {
        return persistencePort.listarProvincias(idDepartamento);
    }
    @Override
    public List<Ubigeo> listarDistritos(String idProvincia) {
        return persistencePort.listarDistritos(idProvincia);
    }
    @Override
    public List<TipoParticipante> listarTiposParticipantes() {
        return persistencePort.listarTiposParticipantes();
    }
}