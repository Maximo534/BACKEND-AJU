package pe.gob.pj.prueba.infraestructure.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.JuezPazEscolarMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarJuezEscolarRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarJuezRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.JuezPazEscolarResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/publico/v1/jueces-escolares")
@RequiredArgsConstructor
public class JuezPazEscolarController {

    private final GestionJuecesEscolaresUseCasePort useCase;
    private final JuezPazEscolarMapper mapper;

    @PostMapping( produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarJuezEscolarRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {

            JuezPazEscolar filtros = mapper.toDomain(request);

            Pagina<JuezPazEscolar> paginaRes = useCase.listar(filtros, pagina, tamanio);

            List<JuezPazEscolarResponse> lista = paginaRes.getContenido().stream()
                    .map(mapper::toResponse).collect(Collectors.toList());

            res.setCodigo("0000");
            res.setDescripcion("Listado exitoso");
            res.setData(lista);
            res.setTotalRegistros(paginaRes.getTotalRegistros());
            res.setTotalPaginas(paginaRes.getTotalPaginas());
            res.setPaginaActual(paginaRes.getPaginaActual());
            res.setTamanioPagina(paginaRes.getTamanioPagina());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error listar", e);
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            JuezPazEscolar juez = useCase.buscarPorId(id);
            if (juez == null) {
                res.setCodigo("404");
                res.setDescripcion("Juez no encontrado");
                return ResponseEntity.status(404).body(res);
            }
            res.setCodigo("200");
            res.setData(mapper.toResponse(juez));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarJuezRequest request,
            @RequestPart(value = "resolucion", required = false) MultipartFile resolucion
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            JuezPazEscolar creado = useCase.registrar(mapper.toDomain(request), resolucion, usuario);

            res.setCodigo("200");
            res.setDescripcion("Juez registrado ID: " + creado.getId());
            res.setData(mapper.toResponse(creado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error registro", e);
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@Valid @ModelAttribute RegistrarJuezRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) throw new Exception("ID obligatorio");

            String usuario = "EMATAMOROSV";
            JuezPazEscolar actualizado = useCase.actualizar(mapper.toDomain(request), usuario);

            res.setCodigo("200");
            res.setDescripcion("Actualizado correctamente");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error actualizar", e);
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(value = "/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> agregarArchivo(
            @RequestParam("idJuez") String idJuez,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            useCase.agregarArchivo(idJuez, archivo, "RESOLUCION_JPE", "EMATAMOROSV");
            res.setCodigo("200");
            res.setDescripcion("Archivo agregado");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @DeleteMapping(value = "/archivos/{nombre:.+}")
    public ResponseEntity<GlobalResponse> eliminarArchivo(@PathVariable String nombre) {
        GlobalResponse res = new GlobalResponse();
        try {
            useCase.eliminarArchivo(nombre);
            res.setCodigo("200");
            res.setDescripcion("Archivo eliminado");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/{id}/resolucion")
    public ResponseEntity<InputStreamResource> descargarResolucion(@PathVariable String id) {
        try {
            RecursoArchivo r = useCase.descargarResolucion(id);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + r.getNombreFileName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(r.getStream()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}