package pe.gob.pj.prueba.infraestructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse; // Importa tu GlobalResponse
import pe.gob.pj.prueba.domain.port.usecase.negocio.masters.GestionarMaestrosUseCasePort;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/publico/v1/maestros")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaestrosController {

    private final GestionarMaestrosUseCasePort useCase;

    @GetMapping("/actividades")
    public ResponseEntity<GlobalResponse> listarActividades() {
        return responder(useCase.listarActividadesOperativas());
    }

    @GetMapping("/indicadores/{idActividad}")
    public ResponseEntity<GlobalResponse> listarIndicadores(@PathVariable String idActividad) {
        return responder(useCase.listarIndicadores(idActividad));
    }

    @GetMapping("/tareas/{idIndicador}")
    public ResponseEntity<GlobalResponse> listarTareas(@PathVariable String idIndicador) {
        return responder(useCase.listarTareas(idIndicador));
    }

    @GetMapping("/distritos-judiciales")
    public ResponseEntity<GlobalResponse> listarDistritosJudiciales() {
        return responder(useCase.listarDistritosJudiciales());
    }

    @GetMapping("/ejes")
    public ResponseEntity<GlobalResponse> listarEjes() {
        return responder(useCase.listarEjes());
    }

    @GetMapping("/materias")
    public ResponseEntity<GlobalResponse> listarMaterias() {
        return responder(useCase.listarMaterias());
    }

    @GetMapping("/tipos-vulnerabilidad")
    public ResponseEntity<GlobalResponse> listarVulnerabilidades() {
        return responder(useCase.listarTiposVulnerabilidad());
    }

    @GetMapping("/tambos/{idCorte}")
    public ResponseEntity<GlobalResponse> listarTambos(@PathVariable String idCorte) {
        return responder(useCase.listarTambos(idCorte));
    }

    @GetMapping("/planes")
    public ResponseEntity<GlobalResponse> buscarPlanes(@RequestParam String idCorte, @RequestParam String periodo) {
        return responder(useCase.buscarPlanes(idCorte, periodo));
    }

    @GetMapping("/ubigeo/departamentos")
    public ResponseEntity<GlobalResponse> listarDepartamentos() {
        return responder(useCase.listarDepartamentos());
    }

    @GetMapping("/ubigeo/provincias/{idDepartamento}")
    public ResponseEntity<GlobalResponse> listarProvincias(@PathVariable String idDepartamento) {
        return responder(useCase.listarProvincias(idDepartamento));
    }

    @GetMapping("/ubigeo/distritos/{idProvincia}")
    public ResponseEntity<GlobalResponse> listarDistritos(@PathVariable String idProvincia) {
        return responder(useCase.listarDistritos(idProvincia));
    }

    @GetMapping("/tipos-participantes")
    public ResponseEntity<GlobalResponse> listarTiposParticipantes() {
        return responder(useCase.listarTiposParticipantes());
    }

    private ResponseEntity<GlobalResponse> responder(List<?> lista) {
        GlobalResponse res = new GlobalResponse();
        try {
            res.setCodigo("0000");
            res.setDescripcion("Operación exitosa");
            res.setData(lista);


            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error al listar maestro", e);
            res.setCodigo("500");
            res.setDescripcion("Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}