package pe.gob.pj.prueba.infraestructure.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Engloba los nombres de los datos usados en los JWT
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum JwtPropiedades {
  
    URL_AUTHENTICATE("url_authenticate","/api/authenticate"),

    //Signing key for HS512 algorithm
    // You can use the page http://www.allkeysgenerator.com/ to generate all kinds of keys
    FIRMA("sign_jwt","eShVmYq3t6w9z$C&F)J@NcRfUjXnZr4u7x!A%D*G-KaPdSgVkYp3s5v8y/B?E(H+MbQeThWmZq4t7w9z$C&F)J@NcRfUjXn2r5u8x/A%D*G-KaPdSgVkYp3s6v9y$B&E"),
    PREFIJO ("jwtprefix","Bearer "),
    
    CLAIM_TIPO("typ","JWT"),
    CLAIM_EMISOR("issuer","secure-api"),
    CLAIM_DESTINO("audience","secure-app"),
    CLAIM_ROLES("X-Roles-Authenticate",""),
    CLAIM_ROL_AUTHENTICATE("X-Rol-Authenticate",""),
    CLAIM_USUARIO_AUTHENTICATE("X-Usuario-Authenticate",""),
    CLAIM_ROL_APLICATIVO("X-Rol-Aplicativo",""),
    CLAIM_USUARIO_APLICATIVO("X-Usuario-Aplicativo",""),
    CLAIM_IP_REALIZA_PETICION("X-Ip-Authenticate",""),
    CLAIM_LIMITE_TOKEN("T-Limite","");
	
	String nombre;
	String valor;
	
}
