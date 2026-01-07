package pe.gob.pj.prueba.infraestructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "timeout")
public record TimeoutProperty(
    Database database,
    Client client) {
    
  public record Database(
      @DefaultValue("120") Integer transaction) {
  }
    
    public record Client(
        Api api) {
      
        public record Api(
            @DefaultValue("60") Integer conection,
            @DefaultValue("60") Integer read) {
        }
        
    }
    
    // Métodos de conveniencia globales
    public Integer getDatabaseTransaction() {
        return database.transaction();
    }
    
    public Integer getClientApiConection() {
      return client.api().conection();
    }
    
    public Integer getClientApiRead() {
      return client.api().read();
    }
}