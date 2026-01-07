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

    private static final long serialVersionUID = 1L;

    private final RegistrarPromocionUseCasePort useCase;
    private final PromocionCulturaMapper mapper;

    @PostMapping(value = "/listar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> listar(
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestBody(required = false) ListarPromocionRequest request
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            PromocionCultura filtrosDominio = new PromocionCultura();
            if (request != null) {
                filtrosDominio.setId(request.getCodigoRegistro());
                filtrosDominio.setDescripcionActividad(request.getDescripcionActividad());
                filtrosDominio.setDistritoJudicialId(request.getDistritoJudicialId());
                filtrosDominio.setFechaInicio(request.getFechaInicio());
                filtrosDominio.setFechaFin(request.getFechaFin());
            }

            Pagina<PromocionCultura> resultadoDominio = useCase.listarPromocion(usuario, filtrosDominio, pagina, tamanio);

            List<PromocionCulturaResponse> contenidoResponse = resultadoDominio.getContenido().stream()
                    .map(d -> PromocionCulturaResponse.builder()
                            .id(d.getId())
                            .nombreActividad(d.getNombreActividad())
                            .fechaInicio(d.getFechaInicio())
                            .fechaFin(d.getFechaFin())
                            .lugarActividad(d.getLugarActividad())
                            .estado(d.getActivo())
                            .build())
                    .collect(Collectors.toList());

            Pagina<PromocionCulturaResponse> paginaResponse = Pagina.<PromocionCulturaResponse>builder()
                    .contenido(contenidoResponse)
                    .totalRegistros(resultadoDominio.getTotalRegistros())
                    .totalPaginas(resultadoDominio.getTotalPaginas())
                    .paginaActual(resultadoDominio.getPaginaActual())
                    .tamanioPagina(resultadoDominio.getTamanioPagina())
                    .build();

            res.setCodigo("200");
            res.setDescripcion("Listado exitoso");
            res.setData(paginaResponse);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al listar Promocion Cultura", e);
            res.setCodigo("500");
            res.setDescripcion("Error al listar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @PostMapping(value = "/registrar-unificado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrarUnificado(
            @RequestPart("datos") RegistrarPromocionRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse response = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV";

            PromocionCultura registrado = useCase.registrarConEvidencias(
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
            log.error("Error en registro unificado Cultura", e);
            response.setCodigo("500");
            response.setDescripcion("Falló el proceso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping(value = "/actualizar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(@RequestBody RegistrarPromocionRequest request) {
        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            PromocionCultura dominio = mapper.toDomain(request);
            // Aseguramos ID manual
            dominio.setId(request.getId());

            PromocionCultura actualizado = useCase.actualizar(dominio, "EMATAMOROSV");

            res.setCodigo("200");
            res.setDescripcion("Actualización de Promoción Cultura exitosa.");
            res.setData(actualizado);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al actualizar Cultura", e);
            res.setCodigo("500");
            res.setDescripcion("Error al actualizar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            PromocionCultura encontrado = useCase.buscarPorId(id);

            res.setCodigo("200");
            res.setDescripcion("Consulta exitosa");
            res.setData(encontrado);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al obtener ID Cultura", e);
            res.setCodigo("500");
            res.setDescripcion("Error al consultar: " + e.getMessage());
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
            log.error("Error al eliminar archivo Cultura", e);
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
            log.error("Error al subir archivo adicional Cultura", e);
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
            log.error("Error descargando anexo Cultura", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/ficha")
    public ResponseEntity<byte[]> descargarFicha(@PathVariable String id) {
        try {
            // Asumimos que agregaste 'generarFichaPdf' a la interfaz del UseCase
            byte[] pdfBytes = useCase.generarFichaPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Ficha_APCJ_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generando ficha Promoción Cultura", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}