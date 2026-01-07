package pe.gob.pj.prueba.infraestructure.client;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.Request;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.properties.TimeoutProperty;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FeignConfig {

  TimeoutProperty timeoutProperties;

  @Bean
  Request.Options feignRequestOptions() {
    return new Request.Options(timeoutProperties.getClientApiConection(), TimeUnit.SECONDS,
        timeoutProperties.getClientApiRead(), TimeUnit.SECONDS, true);
  }

}
