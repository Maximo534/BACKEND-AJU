package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJuezEscolarQuery;

import java.util.List;

public interface GestionJuecesEscolaresUseCasePort {

    Pagina<JuezPazEscolar> listar(String cuo, ListarJuezEscolarQuery query, int pagina, int tamanio);

    JuezPazEscolar registrar(String cuo, JuezPazEscolar dominio, MultipartFile anexo, List<MultipartFile> fotos) throws Exception;

    JuezPazEscolar buscarPorId(String cuo, Long id);

    JuezPazEscolar actualizar(String cuo, JuezPazEscolar dominio) throws Exception;

    void agregarArchivo(String cuo, Long idJuez, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;

    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;

    RecursoArchivo descargarArchivoPorTipo(String cuo, Long idJuez, String tipoArchivo) throws Exception;

    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception;

}