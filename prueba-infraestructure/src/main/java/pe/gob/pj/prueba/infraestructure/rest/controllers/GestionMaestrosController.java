package pe.gob.pj.prueba.infraestructure.rest.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.masters.GestionarMaestrosUseCasePort;
import pe.gob.pj.prueba.infraestructure.common.enums.TipoError;
//import pe.gob.pj.prueba.infraestructure.common.utils.SecurityUtils;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionMaestrosController implements GestionarMaestros, GenerarHttpHeader, MonitorearRequest {

    GestionarMaestrosUseCasePort useCase;

    @Getter AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
    @Getter AuditoriaGeneralMapper auditoriaGeneralMapper;
    @Getter ObjectMapper objectMaper;

    // --- PLANIFICACIÓN ---

    @Override
    public ResponseEntity<GlobalResponse> listarActividades(PeticionServicios peticion) {
        return procesarListado(peticion, () -> useCase.listarActividadesOperativas(peticion.getCuo()));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarIndicadores(PeticionServicios peticion, Long idActividad) {
        return procesarListado(peticion, () -> useCase.listarIndicadores(peticion.getCuo(), idActividad));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarTareas(PeticionServicios peticion, Long idIndicador) {
        return procesarListado(peticion, () -> useCase.listarTareas(peticion.getCuo(), idIndicador));
    }

    // --- ORGANIZACIÓN JUDICIAL ---

    @Override
    public ResponseEntity<GlobalResponse> listarDistritosJudiciales(PeticionServicios peticion) {
        return procesarListado(peticion, () -> useCase.listarDistritosJudiciales(peticion.getCuo()));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarSedesPorCorte(PeticionServicios peticion, Long idCorte) {
        return procesarListado(peticion, () -> useCase.listarSedesPorCorte(peticion.getCuo(), idCorte));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarInstanciasPorSede(PeticionServicios peticion, Long idSede) {
        return procesarListado(peticion, () -> useCase.listarInstanciasPorSede(peticion.getCuo(), idSede));
    }

    // --- MAESTROS GENERALES ---

    @Override
    public ResponseEntity<GlobalResponse> listarEjes(PeticionServicios peticion) {
        return procesarListado(peticion, () -> useCase.listarEjes(peticion.getCuo()));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarMaterias(PeticionServicios peticion) {
        return procesarListado(peticion, () -> useCase.listarMaterias(peticion.getCuo()));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarVulnerabilidades(PeticionServicios peticion) {
        return procesarListado(peticion, () -> useCase.listarTiposVulnerabilidad(peticion.getCuo()));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarTambos(PeticionServicios peticion, Long idCorte) {
        return procesarListado(peticion, () -> useCase.listarTambos(peticion.getCuo(), idCorte));
    }

    @Override
    public ResponseEntity<GlobalResponse> buscarPlanes(PeticionServicios peticion, Long idCorte, String periodo) {
        return procesarListado(peticion, () -> useCase.buscarPlanes(peticion.getCuo(), idCorte, periodo));
    }

    // --- UBIGEO ---

    @Override
    public ResponseEntity<GlobalResponse> listarDepartamentos(PeticionServicios peticion) {
        return procesarListado(peticion, () -> useCase.listarDepartamentos(peticion.getCuo()));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarProvincias(PeticionServicios peticion, Long idDepartamento) {
        return procesarListado(peticion, () -> useCase.listarProvincias(peticion.getCuo(), idDepartamento));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarDistritos(PeticionServicios peticion, Long idProvincia) {
        return procesarListado(peticion, () -> useCase.listarDistritos(peticion.getCuo(), idProvincia));
    }

    // --- PARTICIPANTES Y PERFILES ---

    @Override
    public ResponseEntity<GlobalResponse> listarTiposParticipantes(PeticionServicios peticion) {
        return procesarListado(peticion, () -> useCase.listarTiposParticipantes(peticion.getCuo()));
    }

    @Override
    public ResponseEntity<GlobalResponse> listarPerfiles(PeticionServicios peticion) {
//        Integer idRolLogueado = SecurityUtils.obtenerIdRolUsuario();
        Integer idRolLogueado = 6;
        return procesarListado(peticion, () -> useCase.listarPerfiles(peticion.getCuo(), idRolLogueado));
    }

    private ResponseEntity<GlobalResponse> procesarListado(PeticionServicios peticion, java.util.function.Supplier<List<?>> supplier) {

        var lista = supplier.get();
        GlobalResponse res = new GlobalResponse(peticion.getCuo());
        res.setData(lista);
        guardarAuditoria(Optional.ofNullable(peticion));

        return ResponseEntity.ok(res);
    }
}