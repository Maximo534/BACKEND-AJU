package pe.gob.pj.prueba.infraestructure.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionOrientadorasUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.OrientadoraJudicialMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarOrientadoraRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.OrientadoraJudicialResponse;

@Slf4j
@RestController
@RequestMapping("/publico/v1/orientadoras")
@RequiredArgsConstructor
@Tag(name = "Orientadoras Judiciales", description = "Gestión de atenciones de O.J.")
public class OrientadoraJudicialController {

    private final GestionOrientadorasUseCasePort useCase;
    private final OrientadoraJudicialMapper mapper;

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar Atención OJ", description = "Guarda los datos de la persona atendida, caso y archivos.")
    public ResponseEntity<GlobalResponse> registrar(
            @ModelAttribute RegistrarOrientadoraRequest request,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "foto", required = false) MultipartFile foto
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            String usuario = "EMATAMOROSV"; // Token

            OrientadoraJudicial dominio = mapper.toDomain(request);

            OrientadoraJudicial creado = useCase.registrarAtencion(dominio, anexo, foto, usuario);

            OrientadoraJudicialResponse resp = mapper.toResponse(creado);

            res.setCodigo("200");
            res.setDescripcion("Atención registrada con ID: " + creado.getId());
            res.setData(resp);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error OJ", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}