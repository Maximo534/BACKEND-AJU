package pe.gob.pj.accesojusticia.infraestructure.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.model.common.RecursoArchivo;
import pe.gob.pj.accesojusticia.domain.model.negocio.Documento;
import pe.gob.pj.accesojusticia.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionDocumentosUseCasePort;
import pe.gob.pj.accesojusticia.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.accesojusticia.infraestructure.mappers.DocumentoMapper;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarDocumentoRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.DocumentoResponse;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionDocumentoController implements GestionDocumentos, GenerarHttpHeader, MonitorearRequest {

    GestionDocumentosUseCasePort useCase;
    DocumentoMapper mapper;

    @Getter AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
    @Getter AuditoriaGeneralMapper auditoriaGeneralMapper;
    @Getter ObjectMapper objectMaper;

    @Override
    public ResponseEntity<GlobalResponse> listar(PeticionServicios peticion, String tipo) {
        try {
            List<Documento> listaDominio = useCase.listarDocumentosPorTipo(peticion.getCuo(), tipo);

            List<DocumentoResponse> listaResponse = listaDominio.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Listado de documentos exitoso.");
            response.setData(listaResponse);

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al listar documentos: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> obtenerPorId(PeticionServicios peticion, Long id) {
        try {
            Documento documento = useCase.obtenerDocumento(peticion.getCuo(), id);

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setData(mapper.toResponse(documento));

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener documento: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> registrar(PeticionServicios peticion, RegistrarDocumentoRequest request, MultipartFile archivo) {
        cargarTramaPeticion(peticion, request);

        try {
            Documento dominio = mapper.toDomainRegistrar(request, peticion);

            Documento registrado = useCase.registrarDocumento(peticion.getCuo(), archivo, dominio, peticion.getUsuarioAuth());

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Documento registrado exitosamente.");
            response.setData(mapper.toResponse(registrado));

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar documento: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> actualizar(PeticionServicios peticion, RegistrarDocumentoRequest request, MultipartFile archivo) {
        cargarTramaPeticion(peticion, request);

        try {
            if (request.getId() == null) {
                throw new IllegalArgumentException("El ID es obligatorio para actualizar.");
            }

            Documento dominio = mapper.toDomainActualizar(request.getId(), request, peticion);

            Documento actualizado = useCase.actualizarDocumento(peticion.getCuo(), request.getId(), archivo, dominio, peticion.getUsuarioAuth());

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Documento actualizado correctamente.");
            response.setData(mapper.toResponse(actualizado));

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar documento: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> eliminar(PeticionServicios peticion, Long id) {
        try {
            useCase.eliminarDocumento(peticion.getCuo(), id, peticion.getUsuarioAuth());

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Documento eliminado correctamente (inactivado).");

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar documento: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<Resource> descargar(PeticionServicios peticion, Long id) {
        try {
            RecursoArchivo recurso = useCase.descargarDocumento(peticion.getCuo(), id);

            String nombre = recurso.getNombreFileName().toLowerCase();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

            if (nombre.endsWith(".pdf")) contentType = MediaType.APPLICATION_PDF;
            else if (nombre.endsWith(".doc") || nombre.endsWith(".docx")) contentType = MediaType.valueOf("application/msword");

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getNombreFileName() + "\"")
                    .body(new InputStreamResource(recurso.getStream()));

        } catch (Exception e) {
            throw new RuntimeException("Error descargando archivo: " + e.getMessage());
        }
    }
}