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

    // =========================================================================
    // SECCIÓN 1: GESTIÓN DE JUECES (ALUMNOS)
    // =========================================================================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse> registrarJuez(
            @Valid @RequestPart("data") RegistrarJuezRequest request, // ✅ @Valid
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

}