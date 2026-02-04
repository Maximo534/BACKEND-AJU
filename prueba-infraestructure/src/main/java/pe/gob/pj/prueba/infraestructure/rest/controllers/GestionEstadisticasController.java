package pe.gob.pj.prueba.infraestructure.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.EstadisticasData;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.EstadisticasUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.mappers.EstadisticasMapper;
import pe.gob.pj.prueba.infraestructure.rest.responses.EstadisticasResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionEstadisticasController implements GestionEstadisticas, GenerarHttpHeader, MonitorearRequest {

    EstadisticasUseCasePort useCase;
    EstadisticasMapper mapper;

    @Getter AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
    @Getter AuditoriaGeneralMapper auditoriaGeneralMapper;
    @Getter ObjectMapper objectMaper;

    @Override
    public ResponseEntity<GlobalResponse> obtenerEstadisticas(PeticionServicios peticion, Integer anio) {
        try {
            int anioConsulta = (anio != null) ? anio : LocalDate.now().getYear();

            EstadisticasData dominio = useCase.obtenerEstadisticasCompletas(peticion.getCuo(), anioConsulta);

            EstadisticasResponse dataResponse = mapper.toResponse(dominio);

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Estadísticas globales cargadas correctamente.");
            response.setData(dataResponse);

            guardarAuditoria(Optional.ofNullable(peticion));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Error al generar estadísticas globales: " + e.getMessage(), e);
        }
    }
}