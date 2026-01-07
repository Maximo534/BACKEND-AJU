package pe.gob.pj.prueba.infraestructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"pe.gob.pj.prueba.*"})
@EnableFeignClients
//@EnableConfigurationProperties({SeguridadProperties.class, 
//  TimeoutProperties.class,
//  AlfrescoProperties.class, 
//  CaptchaProperties.class, 
//  ConsultaSigaProperties.class})
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class SpringBootInit extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootInit.class, args);
    }

}