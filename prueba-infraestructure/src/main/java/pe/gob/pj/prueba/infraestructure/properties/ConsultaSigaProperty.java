package pe.gob.pj.prueba.infraestructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "consultasigawsrest")
public record ConsultaSigaProperty(
    String url,
    String usuario,
    String clave,
    String rol,
    String cliente,
    Endpoints endpoints
) {
    
    public record Endpoints(
        String authenticate,
        String consultaestadousuario
    ) {}
    
    // Métodos de conveniencia para URLs completas
    public String getAuthenticateUrl() {
        return buildEndpointUrl(endpoints.authenticate());
    }
    
    public String getConsultaEstadoUsuarioUrl() {
        return buildEndpointUrl(endpoints.consultaestadousuario());
    }
    
    private String buildEndpointUrl(String endpoint) {
        if (url == null || endpoint == null) {
            return null;
        }
        
        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String endpointPath = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        
        return baseUrl + endpointPath;
    }
    
    // Validaciones
    public boolean isConfigured() {
        return url != null && !url.trim().isEmpty() &&
               usuario != null && !usuario.trim().isEmpty() &&
               clave != null && !clave.trim().isEmpty();
    }
    
    // Para logging (sin exponer credenciales)
    public String getConnectionInfo() {
        return String.format("ConsultaSiga[url=%s, usuario=%s, rol=%s, cliente=%s]", 
                           url, 
                           usuario != null ? usuario.substring(0, Math.min(6, usuario.length())) + "..." : "null",
                           rol, 
                           cliente);
    }
    
    // Métodos de acceso directo (compatibilidad con código existente)
    public String getUsuario() {
        return usuario;
    }
    
    public String getClave() {
        return clave;
    }
    
    public String getRol() {
        return rol;
    }
    
    public String getCliente() {
        return cliente;
    }
    
    public String getEndpointAuthenticate() {
        return endpoints.authenticate();
    }
    
    public String getEndpointConsultaEstadoUsuario() {
        return endpoints.consultaestadousuario();
    }
}