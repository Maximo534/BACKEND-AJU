package pe.gob.pj.prueba.domain.port.usecase.negocio.masters;

import pe.gob.pj.prueba.domain.model.negocio.masters.*;
import java.util.List;

public interface GestionarMaestrosUseCasePort {
    List<ActividadOperativa> listarActividadesOperativas();
    List<Indicador> listarIndicadores(String idActividad);
    List<Tarea> listarTareas(String idIndicador);
    List<DistritoJudicial> listarDistritosJudiciales();
    List<Eje> listarEjes();
    List<Materia> listarMaterias();
    List<TipoVulnerabilidad> listarTiposVulnerabilidad();
    List<Tambo> listarTambos(String idCorte);
    List<Plan> buscarPlanes(String idCorte, String periodo);
    List<Ubigeo> listarDepartamentos();
    List<Ubigeo> listarProvincias(String idDepartamento);
    List<Ubigeo> listarDistritos(String idProvincia);
    List<TipoParticipante> listarTiposParticipantes();
}