package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Documento;

public interface DocumentoPersistencePort {

    /**
     * Lista documentos aplicando filtros dinámicos y paginación.
     * @param filtros Objeto de dominio con los campos de búsqueda (tipo, periodo, nombre, etc.)
     * @param pagina Número de página actual (inicia en 1)
     * @param tamanio Cantidad de registros por página
     * @return Objeto Pagina con la lista de documentos y metadatos
     */
    Pagina<Documento> listarConFiltros(Documento filtros, int pagina, int tamanio);

    /**
     * Guarda o Actualiza un documento en la base de datos.
     * @param documento Objeto de dominio a persistir
     * @return El documento guardado (con ID generado si es nuevo)
     */
    Documento guardar(Documento documento) throws Exception;

    /**
     * Busca un documento por su ID único.
     * @param id Identificador del documento
     * @return El documento encontrado o null si no existe
     */
    Documento buscarPorId(String id) throws Exception;
}