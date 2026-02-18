package pe.gob.pj.accesojusticia.domain.exceptions.general;

public class CantidadResultadoNoEsperadoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CantidadResultadoNoEsperadoException(String cantidad) {
    super(cantidad);
  }

}
