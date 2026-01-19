package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;

import java.io.Serializable;

@Data
public class ListarDocumentosRequest implements Serializable {
    private String tipo;       // "RESOLUCION NACIONAL", etc.
    private Integer periodo;   // 2025, 2024...
    private Integer categoriaId;
    private String nombre;     // Búsqueda por coincidencia
}