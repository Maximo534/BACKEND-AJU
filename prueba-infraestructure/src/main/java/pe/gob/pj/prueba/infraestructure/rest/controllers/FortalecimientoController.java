package pe.gob.pj.prueba.infraestructure.rest.controllers;

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
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarFortalecimientoUseCasePort;
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

    private final RegistrarFortalecimientoUseCasePort useCase;
    private final FortalecimientoMapper mapper;

    @PostMapping(value = "/listar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarFfcRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            FortalecimientoCapacidades filtros = FortalecimientoCapacidades.builder().build();

            if (request != null) {
                filtros.setId(request.getCodigoRegistro());
                filtros.setNombreEvento(request.getNombreEvento());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setFechaInicio(request.getFechaInicio());
                filtros.setFechaFin(request.getFechaFin());
            }

            Pagina<FortalecimientoCapacidades> resultado = useCase.listar("EMATAMOROSV", filtros, pagina, tamanio);

            List<FortalecimientoResponse> responseList = resultado.getContenido().stream()
                    .map(d -> FortalecimientoResponse.builder()
                            .id(d.getId())
                            .nombreEvento(d.getNombreEvento())
                            .tipoEvento(d.getTipoEvento())
                            .fechaInicio(d.getFechaInicio())
                            .lugar(d.getNombreInstitucion())
                            .fechaRegistro(d.getFechaRegistro())
                            .estado(d.getActivo())
                            .build())
                    .collect(Collectors.toList());

            Pagina<FortalecimientoResponse> paginaRes = Pagina.<FortalecimientoResponse>builder()
                    .contenido(responseList)
                    .totalRegistros(resultado.getTotalRegistros())
                    .totalPaginas(resultado.getTotalPaginas())
                    .paginaActual(resultado.getPaginaActual())
                    .tamanioPagina(resultado.getTamanioPagina())
                    .build();

            res.setCodigo("200");
            res.setDescripcion("Listado FFC exitoso");
            res.setData(paginaRes);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(value = "/registrar-unificado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @RequestPart("datos") RegistrarFfcRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            FortalecimientoCapacidades registrado = useCase.registrarConEvidencias(
                    mapper.toDomain(request), anexo, videos, fotos, "EMATAMOROSV"
            );
            res.setCodigo("200");
            res.setDescripcion("Registro FFC exitoso. ID: " + registrado.getId());
            res.setData(registrado);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error FFC", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping(value = "/actualizar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@RequestBody RegistrarFfcRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            FortalecimientoCapacidades dominio = mapper.toDomain(request);
            dominio.setId(request.getId());

            FortalecimientoCapacidades actualizado = useCase.actualizar(dominio, "EMATAMOROSV");

            res.setCodigo("200");
            res.setDescripcion("Actualización de datos FFC exitosa.");
            res.setData(actualizado);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al actualizar FFC", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse> buscar(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            res.setData(useCase.buscarPorId(id));
            res.setCodigo("200");
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
            RecursoArchivo r = useCase.descargarAnexo(id, "EMATAMOROSV");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + r.getNombreFileName())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(r.getStream()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
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
            res.setDescripcion("Error al eliminar archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @PostMapping(value = "/archivos/agregar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> agregarArchivo(
            @RequestParam("idEvento") String idEvento,
            @RequestParam("tipo") String tipo,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            useCase.subirArchivoAdicional(idEvento, archivo, tipo, usuario);

            res.setCodigo("200");
            res.setDescripcion("Archivo agregado correctamente");
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion("Error al subir archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
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
            log.error("Error generando ficha FFC", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}