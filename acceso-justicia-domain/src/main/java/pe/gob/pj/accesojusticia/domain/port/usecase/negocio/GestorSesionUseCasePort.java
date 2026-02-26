package pe.gob.pj.accesojusticia.domain.port.usecase.negocio;

import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;

public interface GestorSesionUseCasePort {

    Long registrarIngreso(String cuo, Integer idUsuario, PeticionServicios peticion);
    void registrarSalida(String cuo, Long idSesion, PeticionServicios peticion);

}