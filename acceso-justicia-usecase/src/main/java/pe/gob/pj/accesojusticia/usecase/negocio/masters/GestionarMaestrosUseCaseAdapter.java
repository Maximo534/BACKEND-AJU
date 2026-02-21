package pe.gob.pj.accesojusticia.usecase.negocio.masters;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.accesojusticia.domain.model.negocio.Perfil;
import pe.gob.pj.accesojusticia.domain.model.negocio.masters.*;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.masters.MaestrosPersistencePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.masters.GestionarMaestrosUseCasePort;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionarMaestrosUseCaseAdapter implements GestionarMaestrosUseCasePort {

    private final MaestrosPersistencePort persistencePort;
    private static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<ActividadOperativa> listarActividadesOperativas(String cuo) {
        return persistencePort.listarActividades(cuo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Indicador> listarIndicadores(String cuo, Long idActividad) {
        return persistencePort.listarIndicadoresPorActividad(cuo, idActividad);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Tarea> listarTareas(String cuo, Long idIndicador) {
        return persistencePort.listarTareasPorIndicador(cuo, idIndicador);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public DistritoJudicial obtenerDistritoJudicialPorId(String cuo, Long id) {
        return persistencePort.obtenerDistritoJudicialPorId(cuo, id);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<DistritoJudicial> listarDistritosJudiciales(String cuo) {
        return persistencePort.listarDistritosJudiciales(cuo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Sede> listarSedesPorCorte(String cuo, Long idCorte) {
        return persistencePort.listarSedesPorCorte(cuo, idCorte);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Instancia> listarInstanciasPorSede(String cuo, Long idSede) {
        return persistencePort.listarInstanciasPorSede(cuo, idSede);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Eje> listarEjes(String cuo) {
        return persistencePort.listarEjes(cuo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Materia> listarMaterias(String cuo) {
        return persistencePort.listarMaterias(cuo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<TipoVulnerabilidad> listarTiposVulnerabilidad(String cuo) {
        return persistencePort.listarTiposVulnerabilidad(cuo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Tambo> listarTambos(String cuo, Long idCorte) {
        return persistencePort.listarTambos(cuo, idCorte);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Plan> buscarPlanes(String cuo, Long idCorte, String periodo) {
        return persistencePort.buscarPlanes(cuo, idCorte, periodo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Departamento> listarDepartamentos(String cuo) {
        return persistencePort.listarDepartamentos(cuo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Provincia> listarProvincias(String cuo, Long idDepartamento) {
        return persistencePort.listarProvincias(cuo, idDepartamento);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Distrito> listarDistritos(String cuo, Long idProvincia) {
        return persistencePort.listarDistritos(cuo, idProvincia);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<TipoParticipante> listarTiposParticipantes(String cuo) {
        return persistencePort.listarTiposParticipantes(cuo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public List<Perfil> listarPerfiles(String cuo, String nombrePerfil) {
        return persistencePort.listarPerfilesPermitidos(cuo, nombrePerfil);
    }
}