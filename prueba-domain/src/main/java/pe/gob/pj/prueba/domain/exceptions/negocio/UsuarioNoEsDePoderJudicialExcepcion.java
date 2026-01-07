package pe.gob.pj.prueba.domain.exceptions.negocio;

public class UsuarioNoEsDePoderJudicialExcepcion extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UsuarioNoEsDePoderJudicialExcepcion(String msg) {
    super(msg);
  }

}
