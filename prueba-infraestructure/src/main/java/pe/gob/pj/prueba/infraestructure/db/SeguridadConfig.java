package pe.gob.pj.prueba.infraestructure.db;

import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * 
 * Configuración de conexión,manejo de entidades y sus transacciones a base de datos SEGURIDAD
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "pe.gob.pj.prueba.infraestructure.db.seguridad.repositories",
    entityManagerFactoryRef = "seguridadEntityManagerFactory",
    transactionManagerRef = "txManagerSeguridad")
public class SeguridadConfig {

  // CONEXION CON LA BASE DE DATOS SEGURIDAD
  @Bean(name = "cxSeguridadDS")
  @Primary
  DataSource seguridadDataSource() throws NamingException {
    return (DataSource) new InitialContext()
        .lookup("java:jboss/datasources/servicioPruebaAPISeguridad");
  }

  @Bean(name = "seguridadEntityManagerFactory")
  @Primary
  LocalContainerEntityManagerFactoryBean seguridadEntityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("cxSeguridadDS") DataSource dataSource) {
    return builder.dataSource(dataSource)
        .packages("pe.gob.pj.prueba.infraestructure.db.seguridad.entities")
        .persistenceUnit("seguridad")
        .properties(getHibernateProperties())
        .jta(true)
        .build();
  }

  // Para usar transacciones
  @Primary
  @Bean(name = "txManagerSeguridad")
  PlatformTransactionManager seguridadTransactionManager() {
    return new JtaTransactionManager();
  }

  // Para usar querydsl
  @Bean(name = "seguridadQDSL")
  JPAQueryFactory jpaQueryFactorySeguridad(
      @Qualifier("seguridadEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    return new JPAQueryFactory(em);
  }

  private Map<String, Object> getHibernateProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    properties.put("hibernate.show_sql", "false");
    properties.put("hibernate.format_sql", "true");
    properties.put("hibernate.connection.release_mode", "AFTER_TRANSACTION");
    properties.put("hibernate.type", "true");
    properties.put("hibernate.transaction.jta.platform", "org.hibernate.service.jta.platform.internal.JBossAppServerJtaPlatform");
    return properties;
  }
}
