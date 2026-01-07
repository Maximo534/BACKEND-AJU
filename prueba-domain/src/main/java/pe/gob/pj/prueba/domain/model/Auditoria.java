package pe.gob.pj.prueba.domain.model;

import jakarta.persistence.MappedSuperclass;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class Auditoria {

	String usuario;
	
	String red;

	String nombrePc;

	String numeroIp;

	String direccionMac;
	
}
