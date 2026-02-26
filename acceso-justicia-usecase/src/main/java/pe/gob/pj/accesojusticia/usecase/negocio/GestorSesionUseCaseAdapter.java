package pe.gob.pj.accesojusticia.usecase.negocio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.AccesoPersistenceWritePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestorSesionUseCasePort;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestorSesionUseCaseAdapter implements GestorSesionUseCasePort {

    AccesoPersistenceWritePort accesoWritePort;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long registrarIngreso(String cuo, Integer idUsuario, PeticionServicios peticion) {
        log.info("{} Registrando inicio de sesion para el usuario ID: {}", cuo, idUsuario);
        return accesoWritePort.registrarInicioSesion(cuo, idUsuario, peticion);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void registrarSalida(String cuo, Long idSesion, PeticionServicios peticion) {
        log.info("{} Registrando cierre de sesion para la sesion ID: {}", cuo, idSesion);
        accesoWritePort.registrarCierreSesion(cuo, idSesion, peticion);
    }
}