package pe.gob.pj.prueba.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionDocumentosUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.DocumentoMapper;
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

    @GetMapping
    @Operation(summary = "Listar documentos", description = "Filtra por tipo (c_tipo).")
    public ResponseEntity<GlobalResponse> listar(@RequestParam String tipo) {
        GlobalResponse res = new GlobalResponse();
        try {
            List<Documento> lista = useCase.listarDocumentos(tipo);
            res.setCodigo("200");
            res.setDescripcion("Operación exitosa");
            res.setData(mapper.toResponseList(lista));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error al listar", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar documento", description = "Carga un archivo físico y guarda en BD.")
    public ResponseEntity<GlobalResponse> registrar(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") String tipo,
            @RequestParam("categoriaId") Integer categoriaId
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            Documento creado = useCase.registrarDocumento(archivo, tipo, categoriaId);

            res.setCodigo("200");
            res.setDescripcion("Documento registrado con ID: " + creado.getId());
            res.setData(mapper.toResponse(creado));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error al registrar", e);
            res.setCodigo("500");
            res.setDescripcion("Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}