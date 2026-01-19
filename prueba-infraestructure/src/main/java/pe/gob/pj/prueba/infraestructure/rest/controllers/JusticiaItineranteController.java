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
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJusticiaItineranteUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.JusticiaItineranteMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarItineranteRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFjiRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.JusticiaItineranteResponse;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/publico/v1/justicia-itinerante")
@RequiredArgsConstructor
@Slf4j
public class JusticiaItineranteController implements Serializable {

    private static final long serialVersionUID = 1L;

    private final GestionJusticiaItineranteUseCasePort useCase;
    private final JusticiaItineranteMapper mapper;
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarItineranteRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            JusticiaItinerante filtros = JusticiaItinerante.builder().build();
            if (request != null) {
                filtros.setSearch(request.getSearch());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setFechaInicio(request.getFechaInicio());
                filtros.setFechaFin(request.getFechaFin());
            }

            // Obtener data del dominio
            Pagina<JusticiaItinerante> paginaDominio = useCase.listar(usuario, filtros, pagina, tamanio);

            // Mapear la lista
            List<JusticiaItineranteResponse> listaResponse = paginaDominio.getContenido().stream()
                    .map(mapper::toResponseListado)
                    .collect(Collectors.toList());

            res.setCodigo("0000");
            res.setDescripcion("Listado exitoso");

            // La lista va directo a data
            res.setData(listaResponse);

            // Metadatos de paginación a la raíz
            res.setTotalRegistros(paginaDominio.getTotalRegistros());
            res.setTotalPaginas(paginaDominio.getTotalPaginas());
            res.setPaginaActual(paginaDominio.getPaginaActual());
            res.setTamanioPagina(paginaDominio.getTamanioPagina());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al listar JI", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarFjiRequest request,

            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse response = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            JusticiaItinerante registrado = useCase.registrar(
                    mapper.toDomain(request),
                    anexo,
                    videos,
                    fotos,
                    usuario
            );

            response.setCodigo("200");
            response.setDescripcion("Registro completo (Datos + Archivos) exitoso. ID: " + registrado.getId());
            response.setData(registrado);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en registro unificado", e);
            response.setCodigo("500");
            response.setDescripcion("Falló el proceso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            JusticiaItinerante encontrado = useCase.buscarPorId(id);

            JusticiaItineranteResponse responseDto = mapper.toResponseDetalle(encontrado);

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

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@Valid @ModelAttribute RegistrarFjiRequest request) { // ✅ Agregado @Valid
        GlobalResponse res = new GlobalResponse();
        try {

            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            JusticiaItinerante dominio = mapper.toDomain(request);
            String usuario = "EMATAMOROSV";
            JusticiaItinerante actualizado = useCase.actualizar(dominio, usuario);

            JusticiaItineranteResponse responseDto = mapper.toResponseDetalle(actualizado);

            res.setCodigo("200");
            res.setDescripcion("Actualización exitosa.");
            res.setData(responseDto);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al actualizar FJI", e);
            res.setCodigo("500");
            res.setDescripcion("Error al actualizar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    // El regex :.+ es vital para que no corte el ".jpg" o ".pdf"
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

    @GetMapping("/anexo/{idEvento}")
    public ResponseEntity<InputStreamResource> descargarAnexo(@PathVariable String idEvento) {
        try {
            String usuario = "EMATAMOROSV";

            RecursoArchivo recurso = useCase.descargarAnexo(idEvento, usuario);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + recurso.getNombreFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(recurso.getStream()));

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
            headers.setContentDispositionFormData("inline", "Ficha_JI_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generando PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}