package pe.gob.pj.prueba.infraestructure.rest.controllers;

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
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarBuenaPracticaUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.BuenaPracticaMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.BuenaPracticaResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ResumenEstadisticoResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/publico/v1/buenas-practicas")
@RequiredArgsConstructor
public class BuenaPracticaController {

    private final RegistrarBuenaPracticaUseCasePort useCase;
    private final BuenaPracticaMapper mapper;

    // =========================================================================
    // 1. LISTAR
    // =========================================================================
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarBuenaPracticaRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            BuenaPractica filtros = BuenaPractica.builder().build();
            if (request != null) {
                filtros.setSearch(request.getSearch());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setFechaInicio(request.getFechaInicio());
                filtros.setFechaFin(request.getFechaFin());
            }

            Pagina<BuenaPractica> paginaRes = useCase.listar(usuario, filtros, pagina, tamanio);

            List<BuenaPracticaResponse> listaResponse = paginaRes.getContenido().stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());

            Pagina<BuenaPracticaResponse> resultado = Pagina.<BuenaPracticaResponse>builder()
                    .contenido(listaResponse)
                    .totalRegistros(paginaRes.getTotalRegistros())
                    .totalPaginas(paginaRes.getTotalPaginas())
                    .paginaActual(paginaRes.getPaginaActual())
                    .tamanioPagina(paginaRes.getTamanioPagina())
                    .build();

            res.setCodigo("200");
            res.setDescripcion("Listado exitoso");
            res.setData(resultado);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al listar BP", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // =========================================================================
    // 2. OBTENER POR ID
    // =========================================================================
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            BuenaPractica encontrado = useCase.buscarPorId(id);
            res.setCodigo("200");
            res.setDescripcion("Consulta exitosa");
            res.setData(mapper.toResponse(encontrado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // =========================================================================
    // 3. REGISTRAR
    // =========================================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarBuenaPracticaRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "ppt", required = false) MultipartFile ppt,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            BuenaPractica registrado = useCase.registrar(
                    mapper.toDomain(request),
                    anexo, ppt, fotos, video,
                    usuario
            );

            res.setCodigo("200");
            res.setDescripcion("Registro exitoso. ID: " + registrado.getId());
            res.setData(mapper.toResponse(registrado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error registrando BP", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // =========================================================================
    // 4. ACTUALIZAR
    // =========================================================================
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@Valid @RequestBody RegistrarBuenaPracticaRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar");
            }

            String usuario = "EMATAMOROSV";
            BuenaPractica dominio = mapper.toDomain(request);
            dominio.setId(request.getId());

            BuenaPractica actualizado = useCase.actualizar(dominio, usuario);

            res.setCodigo("200");
            res.setDescripcion("Actualización exitosa");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error actualizando BP", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // =========================================================================
    // 5. GESTIÓN DE ARCHIVOS
    // =========================================================================

    @PostMapping(value = "/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> agregarArchivo(
            @RequestParam("idEvento") String idEvento,
            @RequestParam("tipo") String tipo,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            useCase.agregarArchivo(idEvento, archivo, tipo, usuario);

            res.setCodigo("200");
            res.setDescripcion("Archivo agregado correctamente");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @DeleteMapping(value = "/archivos/{nombre:.+}")
    public ResponseEntity<GlobalResponse> eliminarArchivo(@PathVariable String nombre) {
        GlobalResponse res = new GlobalResponse();
        try {
            useCase.eliminarArchivo(nombre);
            res.setCodigo("200");
            res.setDescripcion("Archivo eliminado correctamente");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // =========================================================================
    // 6. DESCARGAS (Lógica explícita en cada método)
    // =========================================================================

    @GetMapping("/{id}/ficha")
    public ResponseEntity<byte[]> descargarFicha(@PathVariable String id) {
        try {
            byte[] pdfBytes = useCase.generarFichaPdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Ficha_BP_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generando PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<InputStreamResource> descargarDocumento(@PathVariable String id) {
        try {
            RecursoArchivo recurso = useCase.descargarArchivoPorTipo(id, "ANEXO_BP");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + recurso.getNombreFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF) // Forzamos PDF para el anexo
                    .body(new InputStreamResource(recurso.getStream()));

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/ppt")
    public ResponseEntity<InputStreamResource> descargarPpt(@PathVariable String id) {
        try {
            RecursoArchivo recurso = useCase.descargarArchivoPorTipo(id, "PPT_BP");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + recurso.getNombreFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-powerpoint")) // Forzamos PPT
                    .body(new InputStreamResource(recurso.getStream()));

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================================================
    // 7. ESTADÍSTICAS
    // =========================================================================
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
}