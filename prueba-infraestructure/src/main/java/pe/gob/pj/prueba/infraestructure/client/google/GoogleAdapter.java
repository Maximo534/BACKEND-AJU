package pe.gob.pj.prueba.infraestructure.client.google;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.exceptions.general.CaptchaException;
import pe.gob.pj.prueba.domain.model.client.google.response.CaptchaValidResponse;
import pe.gob.pj.prueba.domain.port.client.google.GooglePort;
import pe.gob.pj.prueba.infraestructure.properties.CaptchaProperty;

@Service("googlePort")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GoogleAdapter implements GooglePort {
  
  CaptchaProperty captchaProperties;

  @Override
  public boolean validarCaptcha(String cuo, String token, String remoteIp) {
    boolean resultado = Boolean.FALSE;
    if(Estado.INACTIVO_LETRA.getNombre().equalsIgnoreCase(captchaProperties.aplica())) {
      return Boolean.TRUE;
    }
    UriComponents uriBuilder = UriComponentsBuilder.fromUriString(captchaProperties.url())
        .queryParam("secret", captchaProperties.token()).queryParam("response", token)
        .queryParam("remoteip", remoteIp).build().normalize();
    try {
      RestTemplate plantilla = new RestTemplate();
      var captcha = plantilla.postForObject(uriBuilder.toUriString(), null, CaptchaValidResponse.class);
      if (captcha.getSuccess().equals("true")) {
        return Boolean.TRUE;
      }
    } catch (Exception e) {
      log.info("{} Error al consumir google : {} ", cuo, uriBuilder.toUriString());
      log.error("{} {}", cuo, ProjectUtils.convertExceptionToString(e));
      throw new CaptchaException();
    }
    return resultado;
  }

}
