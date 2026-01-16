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
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarPromocionUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.PromocionCulturaMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarPromocionRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarPromocionRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.PromocionCulturaResponse;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/publico/v1/promocion-cultura")
@RequiredArgsConstructor
@Slf4j
public class PromocionCulturaController implements Serializable {

    private final RegistrarPromocionUseCasePort useCase;
    private final PromocionCulturaMapper mapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarPromocionRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            PromocionCultura filtros = PromocionCultura.builder().build();
            if (request != null) {
                filtros.setSearch(request.getSearch());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setFechaInicio(request.getFechaInicio());
                filtros.setFechaFin(request.getFechaFin());
            }

            // 1. Obtener la data paginada del dominio
            Pagina<PromocionCultura> paginaDominio = useCase.listar(usuario, filtros, pagina, tamanio);

            // 2. Mapear la lista de contenido
            List<PromocionCulturaResponse> listaResponse = paginaDominio.getContenido().stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());

            // ✅ NUEVO: Llenado plano del GlobalResponse
            res.setCodigo("0000"); // Estandarizado
            res.setDescripcion("Listado exitoso");

            // A. La lista va directo a data
            res.setData(listaResponse);

            // B. Metadatos de paginación a la raíz
            res.setTotalRegistros(paginaDominio.getTotalRegistros());
            res.setTotalPaginas(paginaDominio.getTotalPaginas());
            res.setPaginaActual(paginaDominio.getPaginaActual());
            res.setTamanioPagina(paginaDominio.getTamanioPagina());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error listar Cultura", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarPromocionRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse response = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            PromocionCultura registrado = useCase.registrar(mapper.toDomain(request), anexo, videos, fotos, usuario);

            response.setCodigo("200");
            response.setDescripcion("Registro exitoso. ID: " + registrado.getId());
            response.setData(mapper.toResponse(registrado));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error registrar Cultura", e);
            response.setCodigo("500");
            response.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@Valid @ModelAttribute RegistrarPromocionRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null) throw new Exception("ID obligatorio");

            PromocionCultura dominio = mapper.toDomain(request);
            dominio.setId(request.getId());

            PromocionCultura actualizado = useCase.actualizar(dominio, "EMATAMOROSV");

            res.setCodigo("200");
            res.setDescripcion("Actualización exitosa");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error actualizar Cultura", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            PromocionCultura encontrado = useCase.buscarPorId(id);
            res.setCodigo("200");
            res.setData(mapper.toResponse(encontrado)); // Ojo: si necesitas detalle completo, usa otro método en mapper
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion(e.getMessage());
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
            useCase.agregarArchivo(idEvento, archivo, tipo, "EMATAMOROSV");
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
            headers.setContentDispositionFormData("inline", "Ficha_PC_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generando PDF", e);
            return ResponseEntity.internalServerError().build(); // O un 500 simple
        }
    }

}