package pe.gob.pj.prueba.infraestructure.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper; // Importante para convertir JSON string
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarLlapanchikpaqUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.LlapanchikpaqMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarLlapanchikpaqRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.LlapanchikpaqResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/publico/v1/llapanchikpaq")
@RequiredArgsConstructor
@Tag(name = "Llapanchikpaq Justicia", description = "Gestión del módulo LLJ")
public class LlapanchikpaqController {

    private final RegistrarLlapanchikpaqUseCasePort useCase;
    private final LlapanchikpaqMapper mapper;
    private final ObjectMapper jsonMapper; // Inyectar Jackson para convertir string a objeto

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar LLJ", description = "Registra cabecera, detalles y evidencias.")
    public ResponseEntity<GlobalResponse> registrar(
            // Recibimos el objeto JSON como String para evitar problemas con listas en form-data complejo
            @RequestPart("data") String dataJson,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        GlobalResponse res = new GlobalResponse();
        try {
            // 1. Convertir JSON String a Objeto Java
            RegistrarLlapanchikpaqRequest request = jsonMapper.readValue(dataJson, RegistrarLlapanchikpaqRequest.class);

            String usuario = "EMATAMOROSV"; // Token

            // 2. Mapper
            LlapanchikpaqJusticia dominio = mapper.toDomain(request);

            // 3. Caso de Uso
            LlapanchikpaqJusticia creado = useCase.registrar(dominio, anexo, fotos, usuario);

            // 4. Response
            LlapanchikpaqResponse responseData = mapper.toResponse(creado);

            res.setCodigo("200");
            res.setDescripcion("Registro LLJ exitoso. ID: " + creado.getId());
            res.setData(responseData);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("Error LLJ", e);
            res.setCodigo("500");
            res.setDescripcion("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}