package pe.gob.pj.prueba.domain.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pagina<T> {
    private List<T> contenido;
    private long totalRegistros;
    private int totalPaginas;
    private int paginaActual;
    private int tamanioPagina;
}