package pe.gob.pj.accesojusticia.domain.exceptions.general;

public class TiempoActualizarTokenExcedidoException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  public TiempoActualizarTokenExcedidoException(String msg) {
    super(msg);
  }

}
