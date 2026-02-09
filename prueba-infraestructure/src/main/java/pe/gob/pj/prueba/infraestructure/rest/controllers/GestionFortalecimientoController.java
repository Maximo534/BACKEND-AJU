package pe.gob.pj.prueba.infraestructure.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionFortalecimientoUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.mappers.FortalecimientoMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarFfcRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFfcRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.FortalecimientoResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionFortalecimientoController implements GestionFortalecimiento, GenerarHttpHeader, MonitorearRequest {

    GestionFortalecimientoUseCasePort useCase;
    FortalecimientoMapper mapper;

    @Getter AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
    @Getter AuditoriaGeneralMapper auditoriaGeneralMapper;
    @Getter ObjectMapper objectMaper;

    @Override
    public ResponseEntity<GlobalResponse> listar(PeticionServicios peticion, int pagina, int tamanio, ListarFfcRequest filtros) {
        cargarTramaPeticion(peticion, filtros);

        var query = mapper.toQuery(filtros);
        var paginaDominio = useCase.listar(peticion.getCuo(), query, pagina, tamanio);

        List<FortalecimientoResponse> listaResponse = paginaDominio.getContenido().stream()
                .map(mapper::toResponseListado)
                .collect(Collectors.toList());

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(listaResponse);

        response.setTotalRegistros(paginaDominio.getTotalRegistros());
        response.setTotalPaginas(paginaDominio.getTotalPaginas());
        response.setPaginaActual(paginaDominio.getPaginaActual());
        response.setTamanioPagina(paginaDominio.getTamanioPagina());

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> obtenerPorId(PeticionServicios peticion, Long id) {
        FortalecimientoCapacidades encontrado = useCase.buscarPorId(peticion.getCuo(), id);

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(mapper.toResponseDetalle(encontrado));

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> registrar(PeticionServicios peticion, RegistrarFfcRequest request,
                                                    MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) {
        cargarTramaPeticion(peticion, request);

        try {
            // El Mapper inyecta peticion.getUsuarioAuth() en el dominio
            FortalecimientoCapacidades dominio = mapper.toDomainRegistrar(request, peticion);

            FortalecimientoCapacidades registrado = useCase.registrar(peticion.getCuo(), dominio, anexo, videos, fotos);

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Registro exitoso. Código: " + registrado.getCodigo());
            response.setData(mapper.toResponseDetalle(registrado));

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> actualizar(PeticionServicios peticion, RegistrarFfcRequest request) {
        cargarTramaPeticion(peticion, request);

        try {
            if (request.getId() == null) {
                throw new IllegalArgumentException("El ID es obligatorio para actualizar.");
            }

            FortalecimientoCapacidades dominio = mapper.toDomainActualizar(request.getId(), request, peticion);

            FortalecimientoCapacidades actualizado = useCase.actualizar(peticion.getCuo(), dominio);

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Actualización exitosa.");
            response.setData(mapper.toResponseDetalle(actualizado));

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> agregarArchivo(PeticionServicios peticion, Long idEvento, String tipo, MultipartFile archivo) {
        try {
            useCase.agregarArchivo(peticion.getCuo(), idEvento, archivo, tipo, peticion.getUsuarioAuth());

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Archivo agregado correctamente.");

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> eliminarArchivo(PeticionServicios peticion, Long id) {
        try {
            useCase.eliminarArchivo(peticion.getCuo(), id, peticion.getUsuarioAuth());

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Archivo eliminado correctamente.");

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<byte[]> descargarFichaPdf(PeticionServicios peticion, Long id) {
        try {
            byte[] pdfBytes = useCase.generarFichaPdf(peticion.getCuo(), id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Ficha_FFC_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[{}] Error generando PDF FFC", peticion.getCuo(), e);
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> descargarAnexo(PeticionServicios peticion, Long id) {
        try {
            RecursoArchivo recurso = useCase.descargarAnexo(peticion.getCuo(), id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getNombreFileName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(recurso.getStream()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> descargarArchivoPorId(PeticionServicios peticion, Long id) {
        try {
            RecursoArchivo recurso = useCase.descargarArchivoPorId(id);

            String nombre = recurso.getNombreFileName().toLowerCase();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

            if (nombre.endsWith(".pdf")) contentType = MediaType.APPLICATION_PDF;
            else if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) contentType = MediaType.IMAGE_JPEG;
            else if (nombre.endsWith(".png")) contentType = MediaType.IMAGE_PNG;
            else if (nombre.endsWith(".mp4")) contentType = MediaType.valueOf("video/mp4");

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getNombreFileName() + "\"")
                    .body(new InputStreamResource(recurso.getStream()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> exportarExcel(PeticionServicios peticion, ListarFfcRequest filtros) {
        cargarTramaPeticion(peticion, filtros);

        try {
            var query = mapper.toQuery(filtros);

            byte[] excelBytes = useCase.exportarExcel(peticion.getCuo(), query);

            String filename = "Reporte_Fortalecimiento.xlsx";
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(excelBytes));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            log.error("[{}] Error exportando Excel Fortalecimiento: {}", peticion.getCuo(), e.getMessage());
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }
}