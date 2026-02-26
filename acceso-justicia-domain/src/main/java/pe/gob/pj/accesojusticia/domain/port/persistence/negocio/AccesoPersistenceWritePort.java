package pe.gob.pj.accesojusticia.domain.port.persistence.negocio;

import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;

public interface AccesoPersistenceWritePort {

    /**
     * Registra en BD el inicio de sesión y devuelve el ID autogenerado.
     */
    Long registrarInicioSesion(String cuo, Integer idUsuario, PeticionServicios peticion);

    /**
     * Actualiza en BD la fecha de salida de una sesión existente.
     */
    void registrarCierreSesion(String cuo, Long idSesion, PeticionServicios peticion);
}