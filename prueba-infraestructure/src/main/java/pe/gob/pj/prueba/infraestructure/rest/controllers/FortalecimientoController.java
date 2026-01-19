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
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionFortalecimientoUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.FortalecimientoMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarFfcRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFfcRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.FortalecimientoResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/publico/v1/fortalecimiento")
@RequiredArgsConstructor
@Slf4j
public class FortalecimientoController {

    private final GestionFortalecimientoUseCasePort useCase;
    private final FortalecimientoMapper mapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarFfcRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            FortalecimientoCapacidades filtros = FortalecimientoCapacidades.builder().build();
            if (request != null) {
                filtros.setSearch(request.getSearch());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setTipoEvento(request.getTipoEvento());
                filtros.setFechaInicio(request.getFechaInicio());
                filtros.setFechaFin(request.getFechaFin());
            }
            Pagina<FortalecimientoCapacidades> resultado = useCase.listar(usuario, filtros, pagina, tamanio);

            List<FortalecimientoResponse> responseList = resultado.getContenido().stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());

            res.setCodigo("0000");
            res.setDescripcion("Listado exitoso");

            //  La lista va directo a data
            res.setData(responseList);

            // La paginación va a la raíz
            res.setTotalRegistros(resultado.getTotalRegistros());
            res.setTotalPaginas(resultado.getTotalPaginas());
            res.setPaginaActual(resultado.getPaginaActual());
            res.setTamanioPagina(resultado.getTamanioPagina());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al listar FFC", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            FortalecimientoCapacidades encontrado = useCase.buscarPorId(id);

            FortalecimientoResponse responseDto = mapper.toResponse(encontrado);

            res.setCodigo("200");
            res.setDescripcion("Consulta exitosa");
            res.setData(responseDto);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error al consultar por ID", e);
            res.setCodigo("500");
            res.setDescripcion("Error al consultar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarFfcRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            FortalecimientoCapacidades registrado = useCase.registrar(
                    mapper.toDomain(request), anexo, videos, fotos, usuario
            );

            res.setCodigo("200");
            res.setDescripcion("Registro exitoso. ID: " + registrado.getId());
            res.setData(mapper.toResponse(registrado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error FFC", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@Valid @ModelAttribute RegistrarFfcRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            FortalecimientoCapacidades dominio = mapper.toDomain(request);
            dominio.setId(request.getId());

            String usuario = "EMATAMOROSV";
            FortalecimientoCapacidades actualizado = useCase.actualizar(dominio, usuario);

            res.setCodigo("200");
            res.setDescripcion("Actualización exitosa");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error actualizando FFC", e);
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
            RecursoArchivo r = useCase.descargarAnexo(id, "EMATAMOROSV");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + r.getNombreFileName())
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
            headers.setContentDispositionFormData("inline", "Ficha_FFC_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generando ficha", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}