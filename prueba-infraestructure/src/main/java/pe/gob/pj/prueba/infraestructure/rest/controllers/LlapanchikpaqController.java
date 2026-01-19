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
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionLlapanchikpaqUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.LlapanchikpaqMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarLlapanchikpaqRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarLlapanchikpaqRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.LlapanchikpaqResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ResumenEstadisticoResponse;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/publico/v1/llapanchikpaq")
@RequiredArgsConstructor
public class LlapanchikpaqController implements Serializable {

    private static final long serialVersionUID = 1L;

    private final GestionLlapanchikpaqUseCasePort useCase;
    private final LlapanchikpaqMapper mapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarLlapanchikpaqRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            LlapanchikpaqJusticia filtros = LlapanchikpaqJusticia.builder().build();
            if (request != null) {
                filtros.setSearch(request.getSearch());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setFechaInicio(request.getFechaInicio());
                filtros.setFechaFin(request.getFechaFin());
            }

            // Obtener data del dominio
            Pagina<LlapanchikpaqJusticia> paginaDominio = useCase.listar(usuario, filtros, pagina, tamanio);

            // Mapear la lista de contenido
            List<LlapanchikpaqResponse> listaResponse = paginaDominio.getContenido().stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());

            res.setCodigo("0000");
            res.setDescripcion("Listado exitoso");

            // La lista va directo a data
            res.setData(listaResponse);

            // Paginación en la raíz
            res.setTotalRegistros(paginaDominio.getTotalRegistros());
            res.setTotalPaginas(paginaDominio.getTotalPaginas());
            res.setPaginaActual(paginaDominio.getPaginaActual());
            res.setTamanioPagina(paginaDominio.getTamanioPagina());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al listar LLJ", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            LlapanchikpaqJusticia encontrado = useCase.buscarPorId(id);

            if (encontrado == null) {
                res.setCodigo("404");
                res.setDescripcion("Registro no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }

            res.setCodigo("200");
            res.setDescripcion("Consulta exitosa");
            res.setData(mapper.toResponse(encontrado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al consultar por ID", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarLlapanchikpaqRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            LlapanchikpaqJusticia dominio = mapper.toDomain(request);
            LlapanchikpaqJusticia creado = useCase.registrar(dominio, anexo, fotos, usuario);

            res.setCodigo("200");
            res.setDescripcion("Registro exitoso. ID: " + creado.getId());
            res.setData(mapper.toResponse(creado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error registrando LLJ", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@Valid @ModelAttribute RegistrarLlapanchikpaqRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            LlapanchikpaqJusticia dominio = mapper.toDomain(request);
            dominio.setId(request.getId());

            String usuario = "EMATAMOROSV";
            LlapanchikpaqJusticia actualizado = useCase.actualizar(dominio, usuario);

            res.setCodigo("200");
            res.setDescripcion("Actualización exitosa");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error actualizando LLJ", e);
            res.setCodigo("500");
            res.setDescripcion("Error al actualizar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

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
            log.error("Error al subir archivo adicional", e);
            res.setCodigo("500");
            res.setDescripcion("Error al subir archivo: " + e.getMessage());
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
            log.error("Error al eliminar archivo", e);
            res.setCodigo("500");
            res.setDescripcion("Error al eliminar archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @GetMapping("/anexo/{id}")
    public ResponseEntity<InputStreamResource> descargarAnexo(@PathVariable String id) {
        try {
            String usuario = "EMATAMOROSV";
            RecursoArchivo r = useCase.descargarArchivoPorTipo(id, "ANEXO_LLJ");

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

    @GetMapping("/{id}/ficha")
    public ResponseEntity<byte[]> descargarFicha(@PathVariable String id) {
        try {
            byte[] pdfBytes = useCase.generarFichaPdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Ficha_LLJ_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generando PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/estadisticas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerEstadisticasChart() {
        GlobalResponse res = new GlobalResponse();
        try {
            List<ResumenEstadistico> data = useCase.obtenerResumenGrafico();

            List<ResumenEstadisticoResponse> responseList = data.stream()
                    .map(d -> new ResumenEstadisticoResponse(d.getEtiqueta(), d.getCantidad()))
                    .collect(Collectors.toList());

            res.setCodigo("200");
            res.setDescripcion("Estadísticas obtenidas");
            res.setData(responseList);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error estadísticas", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}