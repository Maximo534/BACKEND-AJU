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
 * Configuración de conexión,manejo de entidades y sus transacciones a base de datos PRUEBA
 *
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "pe.gob.pj.prueba.infraestructure.db.negocio.repositories",
    entityManagerFactoryRef = "negocioEntityManagerFactory",
    transactionManagerRef = "txManagerNegocio")
public class NegocioConfig {

  // CONEXION CON LA BASE DE DATOS DE NEGOCIO
  @Bean(name = "cxNegocioDS")
  DataSource negocioDataSource() throws NamingException {
    return (DataSource) new InitialContext()
        .lookup("java:jboss/datasources/servicioPruebaAPINegocio");
  }

  // Para EntityManager
  @Bean(name = "negocioEntityManagerFactory")
  LocalContainerEntityManagerFactoryBean negocioEntityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("cxNegocioDS") DataSource dataSource) {
    return builder.dataSource(dataSource)
        .packages("pe.gob.pj.prueba.infraestructure.db.negocio.entities")
        .persistenceUnit("negocio")
        .properties(getHibernateProperties())
        .jta(true)
        .build();
  }

  // Para usar transacciones
  @Bean(name = "txManagerNegocio")
  PlatformTransactionManager negocioTransactionManager() {
    return new JtaTransactionManager();
  }

  // Para usar querydsl
  @Bean(name = "negocioQDSL")
  JPAQueryFactory jpaQueryFactoryNegocio(
      @Qualifier("negocioEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
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
