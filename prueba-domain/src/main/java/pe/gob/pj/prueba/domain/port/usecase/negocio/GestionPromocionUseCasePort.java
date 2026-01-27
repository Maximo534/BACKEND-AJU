package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;

import java.util.List;

public interface GestionPromocionUseCasePort {

    Pagina<PromocionCultura> listar(String usuario, PromocionCultura filtros, int pagina, int tamanio) throws Exception;

    PromocionCultura registrar(PromocionCultura dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuarioOperacion) throws Exception;

    PromocionCultura actualizar(PromocionCultura dominio, String usuarioOperacion) throws Exception;

    PromocionCultura buscarPorId(String id) throws Exception;

    void eliminarArchivo(String nombreArchivo) throws Exception; // Solo recibe nombre

    void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception;

    RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception;
    byte[] generarFichaPdf(String idEvento) throws Exception;
    RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception;
}