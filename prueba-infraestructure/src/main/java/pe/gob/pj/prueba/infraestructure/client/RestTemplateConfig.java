package pe.gob.pj.prueba.infraestructure.client;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.properties.TimeoutProperty;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RestTemplateConfig {

  TimeoutProperty timeoutProperties;

  @Bean
  RestTemplate restTemplate() throws IOException {
    return new RestTemplate(clientHttpRequestFactory());
  }

  private ClientHttpRequestFactory clientHttpRequestFactory() throws IOException {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(timeoutProperties.getClientApiConection() * 1000);
    factory.setReadTimeout(timeoutProperties.getClientApiRead() * 1000);
    return factory;
  }
}
