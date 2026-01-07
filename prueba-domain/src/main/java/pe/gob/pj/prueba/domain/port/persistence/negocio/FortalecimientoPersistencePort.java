package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;

public interface FortalecimientoPersistencePort {
    FortalecimientoCapacidades guardar(FortalecimientoCapacidades dominio) throws Exception;
    FortalecimientoCapacidades actualizar(FortalecimientoCapacidades dominio) throws Exception;
    FortalecimientoCapacidades obtenerPorId(String id) throws Exception;
    Pagina<FortalecimientoCapacidades> listar(String usuario, FortalecimientoCapacidades filtros, int pagina, int tamanio) throws Exception;
    String obtenerUltimoId() throws Exception;
}