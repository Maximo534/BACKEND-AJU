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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * 
 * Configuración de conexión,manejo de entidades y sus transacciones a base de datos AUDITORIA_GENERAL
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "pe.gob.pj.prueba.infraestructure.db.auditoriageneral.repositories",
    entityManagerFactoryRef = "auditoriaEntityManagerFactory",
    transactionManagerRef = "txManagerAuditoriaGeneral")
public class AuditoriaGeneralConfig {

  // CONEXION AUDITORIA GENERAL
  @Bean(name = "cxAuditoriaGeneralDS")
  DataSource auditoriaDataSource() throws NamingException {
    return (DataSource) new InitialContext()
        .lookup("java:jboss/datasources/servicioPruebaAPIAuditoriaGeneral");
  }

  @Bean(name = "auditoriaEntityManagerFactory")
  LocalContainerEntityManagerFactoryBean auditoriaEntityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("cxAuditoriaGeneralDS") DataSource dataSource) {
    return builder.dataSource(dataSource)
        .packages("pe.gob.pj.prueba.infraestructure.db.auditoriageneral.entities")
        .persistenceUnit("auditoria")
        .properties(getHibernateProperties())
        .jta(true)
        .build();
  }

  @Bean(name = "txManagerAuditoriaGeneral")
  PlatformTransactionManager auditoriaTransactionManager() {
    return new JtaTransactionManager();
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
