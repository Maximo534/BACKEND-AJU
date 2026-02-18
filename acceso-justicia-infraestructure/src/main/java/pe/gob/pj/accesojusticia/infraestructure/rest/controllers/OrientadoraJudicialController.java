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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.model.common.RecursoArchivo;
import pe.gob.pj.accesojusticia.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.accesojusticia.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.accesojusticia.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionOrientadorasUseCasePort;
import pe.gob.pj.accesojusticia.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.accesojusticia.infraestructure.mappers.OrientadoraJudicialMapper;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.ListarOrientadoraRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarOrientadoraRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.OrientadoraJudicialResponse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrientadoraJudicialController implements GestionOrientadoras, GenerarHttpHeader, MonitorearRequest {

    GestionOrientadorasUseCasePort useCase;
    OrientadoraJudicialMapper mapper;

    @Getter AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
    @Getter AuditoriaGeneralMapper auditoriaGeneralMapper;
    @Getter ObjectMapper objectMaper;

    @Override
    public ResponseEntity<GlobalResponse> listar(PeticionServicios peticion, int pagina, int tamanio, ListarOrientadoraRequest filtros) {
        cargarTramaPeticion(peticion, filtros);

        var query = mapper.toQuery(filtros);
        var paginaDominio = useCase.listar(peticion.getCuo(), query, pagina, tamanio);

        List<OrientadoraJudicialResponse> listaResponse = paginaDominio.getContenido().stream()
                .map(mapper::toResponse)
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
        OrientadoraJudicial encontrado = useCase.buscarPorId(peticion.getCuo(), id);

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(mapper.toResponse(encontrado));

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> registrar(PeticionServicios peticion, RegistrarOrientadoraRequest request,
                                                    MultipartFile anexo, List<MultipartFile> fotos) {
        cargarTramaPeticion(peticion, request);

        try {
            OrientadoraJudicial dominio = mapper.toDomainRegistrar(request, peticion);

            OrientadoraJudicial registrado = useCase.registrarAtencion(peticion.getCuo(), dominio, anexo, fotos);

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Registro exitoso. Código: " + registrado.getCodigo());
            response.setData(mapper.toResponse(registrado));

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<GlobalResponse> actualizar(PeticionServicios peticion, RegistrarOrientadoraRequest request) {
        cargarTramaPeticion(peticion, request);

        try {
            if (request.getId() == null) {
                throw new IllegalArgumentException("El ID es obligatorio para actualizar.");
            }

            OrientadoraJudicial dominio = mapper.toDomainActualizar(request.getId(), request, peticion);

            OrientadoraJudicial actualizado = useCase.actualizar(peticion.getCuo(), dominio);

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Actualización exitosa.");
            response.setData(mapper.toResponse(actualizado));

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // =========================================================================================
    // ESTADÍSTICAS
    // =========================================================================================

    @Override
    public ResponseEntity<GlobalResponse> obtenerEstadisticasChart(PeticionServicios peticion) {
        try {
            List<ResumenEstadistico> data = useCase.obtenerResumenGrafico();

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Estadísticas obtenidas correctamente.");
            response.setData(data);

            guardarAuditoria(Optional.ofNullable(peticion));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage(), e);
        }
    }

    // =========================================================================================
    // ARCHIVOS ESPECÍFICOS Y REPORTES
    // =========================================================================================

    @Override
    public ResponseEntity<byte[]> descargarFichaPdf(PeticionServicios peticion, Long id) {
        try {
            byte[] pdfBytes = useCase.generarFichaPdf(peticion.getCuo(), id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Ficha_OJ_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[{}] Error generando PDF Orientadoras", peticion.getCuo(), e);
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> descargarAnexo(PeticionServicios peticion, Long id) {
        try {
            RecursoArchivo recurso = useCase.descargarArchivoPorTipo(peticion.getCuo(), id, "ANEXO");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getNombreFileName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(recurso.getStream()));
        } catch (Exception e) {
            throw new RuntimeException("Error descargando anexo: " + e.getMessage());
        }
    }

    // =========================================================================================
    // ARCHIVOS GENERALES
    // =========================================================================================

    @Override
    public ResponseEntity<GlobalResponse> agregarArchivo(PeticionServicios peticion, Long idCaso, String tipo, MultipartFile archivo) {
        try {
            useCase.agregarArchivo(peticion.getCuo(), idCaso, archivo, tipo, peticion.getUsuarioAuth());

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
    public ResponseEntity<Resource> descargarArchivoPorId(PeticionServicios peticion, Long id) {
        try {
            RecursoArchivo recurso = useCase.descargarArchivoPorId(id);

            String nombre = recurso.getNombreFileName().toLowerCase();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

            if (nombre.endsWith(".pdf")) contentType = MediaType.APPLICATION_PDF;
            else if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) contentType = MediaType.IMAGE_JPEG;
            else if (nombre.endsWith(".png")) contentType = MediaType.IMAGE_PNG;

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getNombreFileName() + "\"")
                    .body(new InputStreamResource(recurso.getStream()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}