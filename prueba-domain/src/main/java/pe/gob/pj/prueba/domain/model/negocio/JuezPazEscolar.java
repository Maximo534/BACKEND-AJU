package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JuezPazEscolar implements Serializable {

    private String id;
    private String dni;
    private String apePaterno;
    private String apeMaterno;
    private String nombres;
    private LocalDate fechaNacimiento;
    private String genero;
    private String grado;
    private String seccion;
    private String email;
    private String celular;
    private String cargo;
    private LocalDate fechaJuramentacion;
    private String resolucionAcreditacion;

    private String institucionEducativaId;

    // Campos de Salida (Para mostrar nombres)
    private String distritoJudicialNombre;
    private String ugelNombre;
    private String institucionEducativaNombre;

    // ✅ NUEVOS: Campos de Entrada (Filtros)
    private String search;              // Buscador general
    private String distritoJudicialId;  // Filtro Corte
    private String ugelId;              // Filtro UGEL

    private String activo;
    private LocalDate fechaRegistro;
    private String usuarioRegistro;

    private List<Archivo> archivosGuardados;
}