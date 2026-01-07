package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JuezPazEscolar implements Serializable {

    private String id;                  // c_cod_reg
    private String dni;
    private String apePaterno;
    private String apeMaterno;
    private String nombres;
    private LocalDate fechaNacimiento;
    private String genero;

    private String grado;               // 1, 2, 3...
    private String seccion;             // A, B, C...

    private String email;
    private String celular;

    private String cargo;               // JUEZ / SECRETARIO
    private LocalDate fechaJuramentacion;
    private String resolucionAcreditacion; // Nro Resolución

    private String institucionEducativaId;
    private String nombreInstitucion;   // Para mostrar en reportes/listados

    private String activo;
    private LocalDate fechaRegistro;
    private String usuarioRegistro;
}