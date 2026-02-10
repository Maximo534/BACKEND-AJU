package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarPromocionQuery;

import java.util.List;

public interface GestionPromocionUseCasePort {

    Pagina<PromocionCultura> listar(String cuo, ListarPromocionQuery query, int pagina, int tamanio, String rolUsuario, String loginUsuario);

    PromocionCultura registrar(String cuo, PromocionCultura dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception;

    PromocionCultura actualizar(String cuo, PromocionCultura dominio) throws Exception;

    PromocionCultura buscarPorId(String cuo, Long id);

    void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception;

    void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;

    RecursoArchivo descargarAnexo(String cuo, Long idEvento) throws Exception;
    byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception;
    RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception ;

    byte[] exportarExcel(String cuo, ListarPromocionQuery query, String rolUsuario, String loginUsuario) throws Exception;
}