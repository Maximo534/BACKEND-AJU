package pe.gob.pj.prueba.domain.port.usecase.negocio.masters;

import java.util.List;

import pe.gob.pj.prueba.domain.model.negocio.Perfil;
import pe.gob.pj.prueba.domain.model.negocio.masters.*;

public interface GestionarMaestrosUseCasePort {

    // --- PLANIFICACIÓN ---
    List<ActividadOperativa> listarActividadesOperativas(String cuo);
    List<Indicador> listarIndicadores(String cuo, Long idActividad);
    List<Tarea> listarTareas(String cuo, Long idIndicador);

    // --- ORGANIZACIÓN JUDICIAL ---
    List<DistritoJudicial> listarDistritosJudiciales(String cuo);
    List<Sede> listarSedesPorCorte(String cuo, Long idCorte);
    List<Instancia> listarInstanciasPorSede(String cuo, Long idSede);

    // --- MAESTROS GENERALES ---
    List<Eje> listarEjes(String cuo);
    List<Materia> listarMaterias(String cuo);
    List<TipoVulnerabilidad> listarTiposVulnerabilidad(String cuo);
    List<Tambo> listarTambos(String cuo, Long idCorte);
    List<Plan> buscarPlanes(String cuo, Long idCorte, String periodo);

    // --- UBIGEO ---
    List<Departamento> listarDepartamentos(String cuo);
    List<Provincia> listarProvincias(String cuo, Long idDepartamento);
    List<Distrito> listarDistritos(String cuo, Long idProvincia);

    // --- PARTICIPANTES Y PERFILES ---
    List<TipoParticipante> listarTiposParticipantes(String cuo);
    List<Perfil> listarPerfiles(String cuo, Integer idRolLogueado);
}