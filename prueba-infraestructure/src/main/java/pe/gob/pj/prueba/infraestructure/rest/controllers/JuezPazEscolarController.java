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
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.JuezPazEscolarMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarCasoRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarJuezRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.JpeCasoAtendidoResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.ResumenEstadisticoResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/publico/v1/jueces-escolares")
@RequiredArgsConstructor
public class JuezPazEscolarController {

    private final GestionJuecesEscolaresUseCasePort useCase;
    private final JuezPazEscolarMapper mapper;

    // ==========================================
    // SECCIÓN 1: GESTIÓN DE JUECES (ALUMNOS)
    // ==========================================

    @PostMapping(value = "/jueces/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrarJuez(
            @RequestPart("data") RegistrarJuezRequest request,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestPart(value = "resolucion", required = false) MultipartFile resolucion
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            JuezPazEscolar creado = useCase.registrarJuez(mapper.toDomain(request), foto, resolucion, usuario);

            res.setCodigo("200");
            res.setDescripcion("Juez escolar registrado con ID: " + creado.getId());
            res.setData(mapper.toResponse(creado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error registrando juez", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    // ==========================================
    // SECCIÓN 2: GESTIÓN DE CASOS (INCIDENTES)
    // ==========================================

    @PostMapping(value = "/casos/listar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listarCasos(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) RegistrarCasoRequest filtrosRequest
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            JpeCasoAtendido filtros = (filtrosRequest != null) ? mapper.toDomain(filtrosRequest) : JpeCasoAtendido.builder().build();
            Pagina<JpeCasoAtendido> paginaRes = useCase.listarCasos(filtros, pagina, tamanio);

            List<JpeCasoAtendidoResponse> listaResponse = mapper.toResponseListCasos(paginaRes.getContenido());

            Pagina<JpeCasoAtendidoResponse> resultado = Pagina.<JpeCasoAtendidoResponse>builder()
                    .contenido(listaResponse)
                    .totalRegistros(paginaRes.getTotalRegistros())
                    .totalPaginas(paginaRes.getTotalPaginas())
                    .paginaActual(paginaRes.getPaginaActual())
                    .tamanioPagina(paginaRes.getTamanioPagina())
                    .build();

            res.setCodigo("200");
            res.setDescripcion("Listado de casos exitoso");
            res.setData(resultado);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error listando casos", e);
            res.setCodigo("500");
            res.setDescripcion("Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(value = "/casos/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrarCaso(
            @RequestPart("data") RegistrarCasoRequest request,
            @RequestPart(value = "acta", required = false) MultipartFile acta,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            JpeCasoAtendido dominio = mapper.toDomain(request);
            JpeCasoAtendido creado = useCase.registrarCaso(dominio, acta, fotos, usuario);

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

    @PutMapping(value = "/casos/actualizar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> actualizarCaso(@RequestBody RegistrarCasoRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null) throw new Exception("ID es obligatorio");
            String usuario = "EMATAMOROSV";
            JpeCasoAtendido dominio = mapper.toDomain(request);
            dominio.setId(request.getId());
            JpeCasoAtendido actualizado = useCase.actualizarCaso(dominio, usuario);

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

    // ✅ DETALLE DEL CASO (Trae metadata de archivos en el JSON)
    @GetMapping(value = "/casos/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerCasoPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            JpeCasoAtendido encontrado = useCase.buscarCasoPorId(id);
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

    // ==========================================
    // SECCIÓN 3: GESTIÓN DE ARCHIVOS Y REPORTES
    // ==========================================

    @PostMapping(value = "/casos/archivos/agregar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> agregarArchivoCaso(
            @RequestParam("idCaso") String idCaso,
            @RequestParam("tipo") String tipo,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";
            useCase.subirArchivoAdicional(idCaso, archivo, tipo, usuario);
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

    @DeleteMapping(value = "/casos/archivos/{nombreArchivo:.+}")
    public ResponseEntity<GlobalResponse> eliminarArchivoCaso(@PathVariable String nombreArchivo) {
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

    // ✅ SOLO DESCARGA POR TIPO (Específicamente para el Acta/Anexo)
    // El frontend llamará a esto cuando den clic en "Descargar Acta"
    @GetMapping("/casos/{id}/acta")
    public ResponseEntity<InputStreamResource> descargarActa(@PathVariable String id) {
        try {
            // "ANEXO_JPE" es el tipo estandarizado para actas
            RecursoArchivo recurso = useCase.descargarArchivoPorTipo(id, "ANEXO_JPE");

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

    // ✅ REPORTE PDF (FICHA)
    @GetMapping("/casos/{id}/reporte")
    public ResponseEntity<byte[]> generarReporteCaso(@PathVariable String id) {
        try {
            byte[] pdfBytes = useCase.generarFichaPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Reporte_Caso_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generando reporte", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/casos/estadisticas/historico", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerEstadisticasChart() {
        GlobalResponse res = new GlobalResponse();
        try {
            // 1. Llamar al Caso de Uso (Devuelve lista del Dominio)
            List<ResumenEstadistico> dataDominio = useCase.obtenerResumenGrafico();

            // 2. Mapear de Dominio -> Response (Tu DTO)
            List<ResumenEstadisticoResponse> dataResponse = dataDominio.stream()
                    .map(d -> ResumenEstadisticoResponse.builder()
                            .etiqueta(d.getEtiqueta()) // Nombre de la Corte
                            .cantidad(d.getCantidad()) // Cantidad de Casos
                            .build())
                    .collect(Collectors.toList());

            // 3. Respuesta Exitosa
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
}