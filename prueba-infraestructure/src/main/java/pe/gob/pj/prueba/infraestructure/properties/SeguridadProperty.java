package pe.gob.pj.prueba.infraestructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "seguridad")
public record SeguridadProperty(
    Dominios dominios,
    String key,
    Aplicativo aplicativo,
    Authenticate authenticate,
    Auditoria auditoria) {
    
    public record Dominios(
        @DefaultValue("*") String permitidos) {
        public String[] getPermitidosArray() {
            return permitidos.split(",");
        }
    }
    
    public record Aplicativo(
        @DefaultValue("0") Integer identificador,
        String nombre) {
    }
    
    public record Authenticate(
        Token token) {
        public record Token(
            @DefaultValue("300") Integer expira,
            @DefaultValue("600") Integer refresh) {
        }
    }
    
    public record Auditoria(
        @DefaultValue("S") String requerida
    ) {
        public boolean isRequerida() {
            return "S".equalsIgnoreCase(requerida);
        }
    }
    
    // Métodos de conveniencia para acceso directo
    public String getSecretKey() {
        return key;
    }
    
    public Integer getAplicativoId() {
        return aplicativo.identificador();
    }
    
    public String getAplicativoNombre() {
        return aplicativo.nombre();
    }
    
    public String[] getDominiosPermitidos() {
        return dominios.getPermitidosArray();
    }
    
    public Integer getTokenExpira() {
        return authenticate.token().expira();
    }
    
    public Integer getTokenRefresh() {
        return authenticate.token().refresh();
    }
    
    public boolean isAuditoriaRequerida() {
        return auditoria.isRequerida();
    }
}