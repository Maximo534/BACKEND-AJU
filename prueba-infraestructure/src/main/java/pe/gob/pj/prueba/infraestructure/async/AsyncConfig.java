package pe.gob.pj.prueba.infraestructure.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;


/**
 * 
 * Configuración para manejo de hilos y asincronía
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class AsyncConfig {

	@Bean(name = "poolTask")
	TaskExecutor taskExecutor() {
	    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	    executor.setCorePoolSize(1); //default: 1
	    executor.setMaxPoolSize(20); //default: Integer.MAX_VALUE
	    executor.setQueueCapacity(1000); // default: Integer.MAX_VALUE
	    executor.setKeepAliveSeconds(180); // default: 60 seconds
	    executor.initialize();
	   return executor;
	}
	
	@Bean
    AsyncUncaughtExceptionHandler uncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Error en el método asíncrono '{}' con parámetros '{}'", method.getName(), params, throwable);
        };
    }
	
}
