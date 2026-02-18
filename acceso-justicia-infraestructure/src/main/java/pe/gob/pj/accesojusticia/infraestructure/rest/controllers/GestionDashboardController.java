package pe.gob.pj.accesojusticia.infraestructure.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.model.negocio.Dashboard;
import pe.gob.pj.accesojusticia.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.DashboardUseCasePort;
import pe.gob.pj.accesojusticia.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.accesojusticia.infraestructure.mappers.DashboardMapper;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.GlobalResponse;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionDashboardController implements GestionDashboard, GenerarHttpHeader, MonitorearRequest {

    DashboardUseCasePort useCase;
    DashboardMapper mapper;

    @Getter AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
    @Getter AuditoriaGeneralMapper auditoriaGeneralMapper;
    @Getter ObjectMapper objectMaper;

    @Override
    public ResponseEntity<GlobalResponse> obtenerDashboard(PeticionServicios peticion, Integer anio) {
        try {
            int anioConsulta = (anio != null) ? anio : LocalDate.now().getYear();

            String usuarioLogueado = peticion.getUsuarioAuth();

            Dashboard dominio = useCase.obtenerDashboard(peticion.getCuo(), anioConsulta, usuarioLogueado);

            GlobalResponse response = new GlobalResponse(peticion.getCuo());
            response.setDescripcion("Dashboard generado correctamente.");
            response.setData(mapper.toResponse(dominio));

            guardarAuditoria(Optional.ofNullable(peticion));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Error al generar dashboard: " + e.getMessage(), e);
        }
    }
}