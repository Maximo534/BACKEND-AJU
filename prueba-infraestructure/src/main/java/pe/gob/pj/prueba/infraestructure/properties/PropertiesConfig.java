package pe.gob.pj.prueba.infraestructure.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SeguridadProperty.class, 
    TimeoutProperty.class,
    AlfrescoProperty.class, 
    CaptchaProperty.class, 
    ConsultaSigaProperty.class})
public class PropertiesConfig {

}
