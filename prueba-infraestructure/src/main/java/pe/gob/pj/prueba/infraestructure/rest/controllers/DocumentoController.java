package pe.gob.pj.prueba.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionDocumentosUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.DocumentoMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarDocumentosRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarDocumentoRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.DocumentoResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/publico/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Documentos", description = "Módulo para cargar Planes, Resoluciones, etc.")
public class DocumentoController {

    private final GestionDocumentosUseCasePort useCase;
    private final DocumentoMapper mapper;

    @GetMapping("/{tipo}")
    public ResponseEntity<GlobalResponse> listar(@PathVariable String tipo) {

        GlobalResponse res = new GlobalResponse();
        try {
            List<Documento> listaDominio = useCase.listarDocumentosPorTipo(tipo);

            List<DocumentoResponse> listaResponse = mapper.toResponseList(listaDominio);

            res.setCodigo("0000");
            res.setDescripcion("Listado exitoso");
            res.setData(listaResponse);

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al listar documentos", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

//    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<GlobalResponse> listar(
//            @RequestParam(defaultValue = "1") int pagina,
//            @RequestParam(defaultValue = "10") int tamanio,
//            @RequestBody(required = false) ListarDocumentosRequest request) {
//
//        GlobalResponse res = new GlobalResponse();
//        try {
//            Documento filtros = Documento.builder().build();
//            if (request != null) {
//                filtros.setTipo(request.getTipo());
//                filtros.setPeriodo(request.getPeriodo());
//                filtros.setCategoriaId(request.getCategoriaId());
//                filtros.setNombre(request.getNombre());
//            }
//
//            Pagina<Documento> paginaDominio = useCase.listarDocumentos(filtros, pagina, tamanio);
//
//            List<DocumentoResponse> listaResponse = mapper.toResponseList(paginaDominio.getContenido());
//
//            res.setCodigo("0000");
//            res.setDescripcion("Listado exitoso");
//            res.setData(listaResponse);
//
//            res.setTotalRegistros(paginaDominio.getTotalRegistros());
//            res.setTotalPaginas(paginaDominio.getTotalPaginas());
//            res.setPaginaActual(paginaDominio.getPaginaActual());
//            res.setTamanioPagina(paginaDominio.getTamanioPagina());
//
//            return ResponseEntity.ok(res);
//
//        } catch (Exception e) {
//            log.error("Error al listar documentos", e);
//            res.setCodigo("500");
//            res.setDescripcion("Error: " + e.getMessage());
//            return ResponseEntity.internalServerError().body(res);
//        }
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrar(
            @Valid @ModelAttribute RegistrarDocumentoRequest request,
            @RequestParam("archivo") MultipartFile archivo) {

        GlobalResponse res = new GlobalResponse();
        try {
            Documento doc = Documento.builder()
                    .tipo(request.getTipo())
                    .categoriaId(request.getCategoriaId())
                    .build();

            Documento creado = useCase.registrarDocumento(archivo, doc);

            res.setCodigo("200");
            res.setDescripcion("Documento registrado exitosamente.");
            res.setData(mapper.toResponse(creado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error al registrar", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
    @GetMapping("/id/{id}")
    @Operation(summary = "Obtener detalle", description = "Retorna los metadatos para llenar el formulario de edición.")
    public ResponseEntity<GlobalResponse> obtenerPorId(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            Documento documento = useCase.obtenerDocumento(id);

            res.setCodigo("200");
            res.setDescripcion("Consulta exitosa.");
            res.setData(mapper.toResponse(documento));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error al obtener documento por ID", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> actualizar(
            @Valid @ModelAttribute RegistrarDocumentoRequest request,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) { // Archivo OPCIONAL

        GlobalResponse res = new GlobalResponse();
        try {
            if (request.getId() == null || request.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            Documento datos = Documento.builder()
                    .tipo(request.getTipo())
                    .categoriaId(request.getCategoriaId())
                    .build();

            Documento actualizado = useCase.actualizarDocumento(request.getId(), archivo, datos);

            res.setCodigo("200");
            res.setDescripcion("Documento actualizado correctamente.");
            res.setData(mapper.toResponse(actualizado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error al actualizar", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<InputStreamResource> descargar(@PathVariable String id) {
        try {
            RecursoArchivo recurso = useCase.descargarDocumento(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getNombreFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(recurso.getStream()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse> eliminar(@PathVariable String id) {
        GlobalResponse res = new GlobalResponse();
        try {
            useCase.eliminarDocumento(id);
            res.setCodigo("200");
            res.setDescripcion("Documento eliminado.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}