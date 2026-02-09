package pe.gob.pj.prueba.infraestructure.rest.controllers;

import java.util.Optional;
import java.util.stream.Collectors;

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
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionUsuarioUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.mappers.UsuarioMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.CambiarClaveRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.GlobalResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.UsuarioSesionResponse;
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

        var paginaDominio = useCase.listar(peticion.getCuo(), mapper.toQuery(filtros), pagina, tamanio);

        var listaResponse = paginaDominio.getContenido().stream()
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

        var usuarioNuevo = useCase.registrar(peticion.getCuo(), mapper.toUsuario(request, peticion), peticion.getUsuario());

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

    @Override
    public ResponseEntity<GlobalResponse> resetearClave(PeticionServicios peticion, Integer id) {

        useCase.resetearClave(
                peticion.getCuo(),
                id,
                peticion.getRol(),
                peticion.getUsuarioAuth()
        );

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setDescripcion("La contraseña ha sido reseteada correctamente.");

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> cambiarContrasena(PeticionServicios peticion, CambiarClaveRequest request) {

        useCase.cambiarContrasenaPropia(
                peticion.getCuo(),
                peticion.getUsuario(),
                request.getClaveActual(),
                request.getNuevaClave(),
                request.getConfirmarClave()
        );

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setDescripcion("Su contraseña ha sido actualizada correctamente.");

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GlobalResponse> obtenerPerfilSesion(PeticionServicios peticion) {

        Usuario usuarioDomain = useCase.obtenerDatosSesion(peticion.getCuo(), peticion.getUsuario());

        UsuarioSesionResponse data = mapper.toSesionResponse(usuarioDomain);

        GlobalResponse response = new GlobalResponse(peticion.getCuo());
        response.setData(data);

        guardarAuditoria(Optional.ofNullable(peticion));
        return ResponseEntity.ok(response);
    }
}