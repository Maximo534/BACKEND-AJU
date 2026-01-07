package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarJuezRequest implements Serializable {

    private String dni;
    private String apePaterno;
    private String apeMaterno;
    private String nombres;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;
    private String genero;
    private String grado;
    private String seccion;
    private String email;
    private String celular;
    private String cargo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaJuramentacion;
    private String resolucionAcreditacion;
    private String institucionEducativaId;
}