package pe.gob.pj.prueba.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionOrientadorasUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.OrientadoraJudicialMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarOrientadoraRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarOrientadoraRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.OrientadoraJudicialResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ResumenEstadisticoResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/publico/v1/orientadoras")
@RequiredArgsConstructor
@Tag(name = "Orientadoras Judiciales", description = "Gestión de atenciones de O.J.")
public class OrientadoraJudicialController {

    private final GestionOrientadorasUseCasePort useCase;
    private final OrientadoraJudicialMapper mapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarOrientadoraRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            OrientadoraJudicial filtros = OrientadoraJudicial.builder().build();
            if(request != null) {
                filtros.setSearch(request.getSearch());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setFechaAtencion(request.getFechaInicio());
            }

            //  Obtener data del dominio
            Pagina<OrientadoraJudicial> pag = useCase.listar(usuario, filtros, pagina, tamanio);

            // Mapear a la lista de respuesta
            List<OrientadoraJudicialResponse> listRes = pag.getContenido().stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());


            // Estructura plana
            res.setCodigo("0000"); // Estandarizado
            res.setDescripcion("Listado exitoso");

            //La lista directa a data
            res.setData(listRes);

            //Metadatos de paginación a la raíz
            res.setTotalRegistros(pag.getTotalRegistros());
            res.setTotalPaginas(pag.getTotalPaginas());
            res.setPaginaActual(pag.getPaginaActual());
            res.setTamanioPagina(pag.getTamanioPagina());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error listar OJ", e);
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarOrientadoraRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            OrientadoraJudicial dominio = mapper.toDomain(request);

            OrientadoraJudicial creado = useCase.registrarAtencion(dominio, anexo, fotos, usuario);

            res.setCodigo("200");
            res.setDescripcion("Registrado ID: " + creado.getId());
            res.setData(mapper.toResponse(creado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error OJ", e);
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@Valid @ModelAttribute RegistrarOrientadoraRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar");
            }

            String usuario = "EMATAMOROSV";
            OrientadoraJudicial dominio = mapper.toDomain(request);

            OrientadoraJudicial actualizado = useCase.actualizar(dominio, usuario);

            res.setCodigo("200");
            res.setDescripcion("Registro actualizado correctamente");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error actualizando OJ", e);
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse> obtener(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            OrientadoraJudicial oj = useCase.buscarPorId(id);
            if (oj == null) {
                res.setCodigo("404");
                res.setDescripcion("Registro no encontrado");
                return ResponseEntity.status(404).body(res);
            }
            res.setCodigo("200");
            res.setData(mapper.toResponse(oj));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(value = "/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> agregarArchivo(
            @RequestParam("idCaso") String idCaso,
            @RequestParam("tipo") String tipo,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            useCase.agregarArchivo(idCaso, archivo, tipo, "EMATAMOROSV");
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
            res.setDescripcion("Eliminado");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/anexo/{id}")
    public ResponseEntity<InputStreamResource> descargarAnexo(@PathVariable String id) {
        try {
            RecursoArchivo r = useCase.descargarAnexo(id, "ANEXO_OJ");
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

    @GetMapping(value = "/estadisticas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerEstadisticasChart() {
        GlobalResponse res = new GlobalResponse();
        try {
            List<ResumenEstadistico> dataDominio = useCase.obtenerResumenGrafico();

            List<ResumenEstadisticoResponse> dataResponse = dataDominio.stream()
                    .map(d -> ResumenEstadisticoResponse.builder()
                            .etiqueta(d.getEtiqueta())
                            .cantidad(d.getCantidad())
                            .build())
                    .collect(Collectors.toList());

            res.setCodigo("200");
            res.setDescripcion("Estadísticas obtenidas");
            res.setData(dataResponse);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error estadísticas", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @GetMapping("/{id}/ficha")
    public ResponseEntity<byte[]> descargarFicha(@PathVariable String id) {
        try {
            byte[] pdfBytes = useCase.generarFichaPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Ficha_OJ_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generando PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}