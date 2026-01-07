package pe.gob.pj.prueba.infraestructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "captcha")
public record CaptchaProperty(
    @DefaultValue("https://www.google.com/recaptcha/api/siteverify") String url,
    String token,
    String aplica
) {
    
    // Métodos de validación
    public boolean isConfigured() {
        return token != null && !token.trim().isEmpty() && 
               url != null && !url.trim().isEmpty();
    }
    
    public boolean isGoogleRecaptcha() {
        return url != null && url.contains("google.com/recaptcha");
    }
    
    // Para debugging (sin exponer el token completo)
    public String getTokenMasked() {
        if (token == null || token.length() < 10) {
            return "***INVALID***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
    
    public String getConfigInfo() {
        return String.format("Captcha[url=%s, aplica=%s, token=%s]", url, aplica, getTokenMasked());
    }
}