package pe.gob.pj.prueba.domain.port.client.google;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface GooglePort {

  /**
   * Método para validar el captcha utilizado de google
   * 
   * @param cuo String código único de operación
   * @param token String token enviado para la validación
   * @param remoteIp String ip remota de donde consumo
   * @return true se valida o false si rechaza
   */
  boolean validarCaptcha(String cuo, String token, String remoteIp);

}
