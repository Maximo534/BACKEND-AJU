package pe.gob.pj.prueba.domain.exceptions.general;

import java.io.Serializable;

public class DescargaArchivoAlfrescoFallidoException extends RuntimeException
    implements Serializable {

  private static final long serialVersionUID = 1L;

  public DescargaArchivoAlfrescoFallidoException(String msg) {
    super(msg);
  }

}
