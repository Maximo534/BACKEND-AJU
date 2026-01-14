package pe.gob.pj.prueba.domain.port.persistence.negocio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;
import java.util.stream.Collectors;

public interface LlapanchikpaqPersistencePort {
    LlapanchikpaqJusticia guardar(LlapanchikpaqJusticia dominio) throws Exception;
    String obtenerUltimoId() throws Exception;
    LlapanchikpaqJusticia actualizar(LlapanchikpaqJusticia dominio) throws Exception;
    Pagina<LlapanchikpaqJusticia> listar(String usuario,LlapanchikpaqJusticia filtros, int pagina, int tamanio) throws Exception;
    LlapanchikpaqJusticia buscarPorId(String id) throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}