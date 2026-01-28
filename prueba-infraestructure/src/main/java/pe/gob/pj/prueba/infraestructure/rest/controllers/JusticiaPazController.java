package pe.gob.pj.prueba.infraestructure.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJusticiaPazUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.JuezPazEscolarMapper;
import pe.gob.pj.prueba.infraestructure.mappers.JusticiaPazMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarJpeCasosRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarCasoRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.JpeCasoAtendidoResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ResumenEstadisticoResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/publico/v1/justicia-paz")
@RequiredArgsConstructor
public class JusticiaPazController {

    private final GestionJusticiaPazUseCasePort useCase;
    private final JusticiaPazMapper mapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarJpeCasosRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            JpeCasoAtendido filtros = JpeCasoAtendido.builder().build();

            if (request != null) {
                filtros.setSearch(request.getSearch());
                filtros.setDistritoJudicialId(request.getDistritoJudicialId());
                filtros.setUgelId(request.getUgelId());
                filtros.setInstitucionEducativaId(request.getInstitucionEducativaId());
                filtros.setFechaRegistro(request.getFechaRegistro());
            }

            // Obtener la data paginada del servicio
            Pagina<JpeCasoAtendido> paginaRes = useCase.listar(usuario, filtros, pagina, tamanio);

            // Mapear la lista
            List<JpeCasoAtendidoResponse> listaResponse = paginaRes.getContenido().stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());


            res.setCodigo("0000");
            res.setDescripcion("Listado de casos exitoso");

            // La lista va directo a data
            res.setData(listaResponse);

            // La paginación va a la raíz
            res.setTotalRegistros(paginaRes.getTotalRegistros());
            res.setTotalPaginas(paginaRes.getTotalPaginas());
            res.setPaginaActual(paginaRes.getPaginaActual());
            res.setTamanioPagina(paginaRes.getTamanioPagina());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error listando casos", e);
            res.setCodigo("500");
            res.setDescripcion("Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            JpeCasoAtendido encontrado = useCase.buscarPorId(id);
            if (encontrado == null) {
                res.setCodigo("404");
                res.setDescripcion("Caso no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }
            res.setCodigo("200");
            res.setDescripcion("Consulta exitosa");
            res.setData(mapper.toResponse(encontrado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error obteniendo caso", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @RequestPart("data") @Valid  RegistrarCasoRequest request,
            @RequestPart(value = "acta", required = false) MultipartFile acta,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            JpeCasoAtendido dominio = mapper.toDomain(request);
            JpeCasoAtendido creado = useCase.registrar(dominio, acta, fotos, usuario);

            res.setCodigo("200");
            res.setDescripcion("Caso registrado correctamente. ID: " + creado.getId());
            res.setData(mapper.toResponse(creado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error registrando caso", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@RequestPart("data") @Valid RegistrarCasoRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("ID es obligatorio para actualizar");
            }
            String usuario = "EMATAMOROSV";
            JpeCasoAtendido dominio = mapper.toDomain(request);
            JpeCasoAtendido actualizado = useCase.actualizar(dominio, usuario);

            res.setCodigo("200");
            res.setDescripcion("Caso actualizado correctamente");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error actualizando caso", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(value = "/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> agregarArchivo(
            @RequestParam("idCaso") String idCaso,
            @RequestParam("tipo") String tipo, // "ACTA_JPE" o "FOTO_JPE"
            @RequestPart("archivo") MultipartFile archivo
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            useCase.agregarArchivo(idCaso, archivo, tipo, usuario);
            res.setCodigo("200");
            res.setDescripcion("Archivo agregado correctamente");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error agregando archivo", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @DeleteMapping(value = "/archivos/{nombreArchivo:.+}")
    public ResponseEntity<GlobalResponse> eliminarArchivo(@PathVariable String nombreArchivo) {
        GlobalResponse res = new GlobalResponse();
        try {
            useCase.eliminarArchivo(nombreArchivo);
            res.setCodigo("200");
            res.setDescripcion("Archivo eliminado correctamente");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error eliminando archivo", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/{id}/acta")
    public ResponseEntity<InputStreamResource> descargarActa(@PathVariable String id) {
        try {
            RecursoArchivo recurso = useCase.descargarArchivoPorTipo(id, "ACTA_JPE");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + recurso.getNombreFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(recurso.getStream()));

        } catch (Exception e) {
            log.error("Error descargando acta", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("{id}/ficha")
    public ResponseEntity<byte[]> generarReporte(@PathVariable String id) {
        try {
            byte[] pdfBytes = useCase.generarFichaPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Ficha_Caso_JPE_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generando reporte", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "estadisticas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerEstadisticasChart() {
        GlobalResponse res = new GlobalResponse();
        try {
            List<ResumenEstadistico> dataDominio = useCase.obtenerResumenGrafico();

            List<ResumenEstadisticoResponse> dataResponse = dataDominio.stream()
                    .map(d -> new ResumenEstadisticoResponse(d.getEtiqueta(), d.getCantidad()))
                    .collect(Collectors.toList());

            res.setCodigo("200");
            res.setDescripcion("Estadísticas históricas obtenidas correctamente");
            res.setData(dataResponse);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error obteniendo estadísticas", e);
            res.setCodigo("500");
            res.setDescripcion("Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/descargar/archivo/{nombre:.+}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String nombre) {
        try {
            RecursoArchivo recurso = useCase.descargarArchivoPorNombre(nombre);

            String nombreLower = nombre.toLowerCase();
            String contentType = "application/octet-stream";

            if (nombreLower.endsWith(".jpg") || nombreLower.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (nombreLower.endsWith(".png")) contentType = "image/png";
            else if (nombreLower.endsWith(".mp4")) contentType = "video/mp4";
            else if (nombreLower.endsWith(".pdf")) contentType = "application/pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getNombreFileName() + "\"")
                    .body(new InputStreamResource(recurso.getStream()));

        } catch (Exception e) {
            log.error("Error descargando archivo Justicia Paz: {}", nombre, e);
            return ResponseEntity.notFound().build();
        }
    }

}