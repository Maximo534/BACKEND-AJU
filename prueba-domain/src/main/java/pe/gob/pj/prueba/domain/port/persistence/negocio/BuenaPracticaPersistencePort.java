package pe.gob.pj.prueba.domain.port.persistence.negocio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;
import java.util.stream.Collectors;

public interface BuenaPracticaPersistencePort {
    Pagina<BuenaPractica> listar(String usuario, BuenaPractica filtros, int pagina, int tamanio) throws Exception;
    BuenaPractica actualizar(BuenaPractica dominio) throws Exception;
    BuenaPractica buscarPorId(String id) throws Exception;
    BuenaPractica guardar(BuenaPractica dominio) throws Exception;
    String obtenerUltimoId() throws Exception;

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}