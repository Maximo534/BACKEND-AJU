package pe.gob.pj.prueba.infraestructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.prueba.domain.model.negocio.masters.*;
import pe.gob.pj.prueba.domain.port.usecase.negocio.masters.GestionarMaestrosUseCasePort;

import java.util.List;

@RestController
@RequestMapping("/publico/v1/maestros")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaestrosController {

    private final GestionarMaestrosUseCasePort useCase;

    // --- ACTIVIDADES OPERATIVAS ---
    @GetMapping("/actividades")
    public ResponseEntity<List<ActividadOperativa>> listarActividades() {
        return ResponseEntity.ok(useCase.listarActividadesOperativas());
    }
    @GetMapping("/indicadores/{idActividad}")
    public ResponseEntity<List<Indicador>> listarIndicadores(@PathVariable String idActividad) {
        return ResponseEntity.ok(useCase.listarIndicadores(idActividad));
    }
    @GetMapping("/tareas/{idIndicador}")
    public ResponseEntity<List<Tarea>> listarTareas(@PathVariable String idIndicador) {
        return ResponseEntity.ok(useCase.listarTareas(idIndicador));
    }

    // --- NUEVOS MAESTROS ---
    @GetMapping("/distritos-judiciales")
    public ResponseEntity<List<DistritoJudicial>> listarDistritosJudiciales() {
        return ResponseEntity.ok(useCase.listarDistritosJudiciales());
    }

    @GetMapping("/ejes")
    public ResponseEntity<List<Eje>> listarEjes() {
        return ResponseEntity.ok(useCase.listarEjes());
    }

    @GetMapping("/materias")
    public ResponseEntity<List<Materia>> listarMaterias() {
        return ResponseEntity.ok(useCase.listarMaterias());
    }

    @GetMapping("/tipos-vulnerabilidad")
    public ResponseEntity<List<TipoVulnerabilidad>> listarVulnerabilidades() {
        return ResponseEntity.ok(useCase.listarTiposVulnerabilidad());
    }

    // Dependientes de Corte
    @GetMapping("/tambos/{idCorte}")
    public ResponseEntity<List<Tambo>> listarTambos(@PathVariable String idCorte) {
        return ResponseEntity.ok(useCase.listarTambos(idCorte));
    }

    @GetMapping("/planes")
    public ResponseEntity<List<Plan>> buscarPlanes(@RequestParam String idCorte, @RequestParam String periodo) {
        return ResponseEntity.ok(useCase.buscarPlanes(idCorte, periodo));
    }

    // --- UBIGEO ---
    @GetMapping("/ubigeo/departamentos")
    public ResponseEntity<List<Ubigeo>> listarDepartamentos() {
        return ResponseEntity.ok(useCase.listarDepartamentos());
    }

    @GetMapping("/ubigeo/provincias/{idDepartamento}")
    public ResponseEntity<List<Ubigeo>> listarProvincias(@PathVariable String idDepartamento) {
        return ResponseEntity.ok(useCase.listarProvincias(idDepartamento));
    }

    @GetMapping("/ubigeo/distritos/{idProvincia}")
    public ResponseEntity<List<Ubigeo>> listarDistritos(@PathVariable String idProvincia) {
        return ResponseEntity.ok(useCase.listarDistritos(idProvincia));
    }

    @GetMapping("/tipos-participantes")
    public ResponseEntity<List<TipoParticipante>> listarTiposParticipantes() {
        return ResponseEntity.ok(useCase.listarTiposParticipantes());
    }
}