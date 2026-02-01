package pe.gob.pj.prueba.infraestructure.rest.controllers;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionUsuarioUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.mappers.UsuarioMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.VerificarLoginResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionarUsuarioController implements GestionarUsuario, GenerarHttpHeader, MonitorearRequest {

    GestionUsuarioUseCasePort useCase;
    UsuarioMapper mapper;

    @Getter AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
    @Getter AuditoriaGeneralMapper auditoriaGeneralMapper;
    @Getter ObjectMapper objectMaper;

    @Override
    public ResponseEntity<GlobalResponse> listar(PeticionServicios peticion, int pagina, int tamanio, ListarUsuarioRequest filtros) {
        cargarTramaPeticion(peticion, filtros);

        var res = useCase.listar(peticion.getCuo(), mapper.toQuery(filtros), pagina, tamanio);

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(res);
        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> obtenerPorId(PeticionServicios peticion, Integer id) {
        var usuario = useCase.buscarPorId(peticion.getCuo(), id);

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(mapper.toUsuarioResponse(usuario));

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> registrar(PeticionServicios peticion, RegistrarUsuarioRequest request) {
        cargarTramaPeticion(peticion, request);

        var usuarioNuevo = useCase.registrar(peticion.getCuo(), mapper.toUsuario(request, peticion));

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(mapper.toUsuarioResponse(usuarioNuevo));

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> actualizar(PeticionServicios peticion, RegistrarUsuarioRequest request) {
        cargarTramaPeticion(peticion, request);

        var usuarioActualizado = useCase.actualizar(peticion.getCuo(), mapper.toUsuario(request, peticion));

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setDescripcion("Actualización exitosa");
        response.setData(mapper.toUsuarioResponse(usuarioActualizado));

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> verificarLogin(PeticionServicios peticion, String login) {
        boolean disponible = useCase.verificarDisponibilidadLogin(peticion.getCuo(), login);

        VerificarLoginResponse data = new VerificarLoginResponse(
                login,
                disponible,
                disponible ? "El usuario está disponible." : "El usuario ya existe, por favor intente con otro."
        );

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(data);

        guardarAuditoria(Optional.ofNullable(peticion));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> cambiarEstado(PeticionServicios peticion, Integer id, String activo) {

        var usuarioAudit = mapper.toUsuarioEstado(id, activo, peticion);

        useCase.cambiarEstado(peticion.getCuo(), usuarioAudit);

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setDescripcion("El estado del usuario se actualizó correctamente.");

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }
}