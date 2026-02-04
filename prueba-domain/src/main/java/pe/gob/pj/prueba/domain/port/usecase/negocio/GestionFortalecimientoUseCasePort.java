package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarFortalecimientoQuery;

import java.util.List;

public interface GestionFortalecimientoUseCasePort {

    Pagina<FortalecimientoCapacidades> listar(String cuo, ListarFortalecimientoQuery query, int pagina, int tamanio);

    FortalecimientoCapacidades registrar(String cuo, FortalecimientoCapacidades dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception ;
    FortalecimientoCapacidades buscarPorId(String cuo, Long id);

    FortalecimientoCapacidades actualizar(String cuo, FortalecimientoCapacidades dominio) throws Exception;

    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;
    void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception ;
    RecursoArchivo descargarAnexo(String cuo, Long idEvento) throws Exception;
    byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception;
    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception;
}