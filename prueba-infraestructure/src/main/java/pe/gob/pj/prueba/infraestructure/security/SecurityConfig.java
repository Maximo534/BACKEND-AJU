package pe.gob.pj.prueba.infraestructure.security;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.port.usecase.seguridad.AutenticarClienteUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.seguridad.ObtenerInfoClienteUseCasePort;
import pe.gob.pj.prueba.infraestructure.common.utils.EncryptUtils;
import pe.gob.pj.prueba.infraestructure.common.utils.InfraestructureConstant;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;
import pe.gob.pj.prueba.infraestructure.security.adapters.UserDetailsServiceAdapter;
import pe.gob.pj.prueba.infraestructure.security.filters.AuditValidationFilter;
import pe.gob.pj.prueba.infraestructure.security.filters.JwtAuthorizationFilter;
import pe.gob.pj.prueba.infraestructure.security.filters.JwtLoginAuthenticationFilter;
import pe.gob.pj.prueba.usecase.seguridad.ValidarAutorizacionUseCaseAdapter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SecurityConfig {

  ObtenerInfoClienteUseCasePort obtenerInfoClienteUseCasePort;
  AutenticarClienteUseCasePort autenticarClienteUseCasePort;
  ValidarAutorizacionUseCaseAdapter validarAutorizacionUseCaseAdapter;
  SeguridadProperty configuracionSeguridadProperties;
  EncryptUtils encryptUtils;

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(
        Arrays.asList(configuracionSeguridadProperties.dominios().getPermitidosArray()));
    configuration.setAllowedMethods(Arrays.asList(HttpMethod.OPTIONS.name(), HttpMethod.GET.name(),
        HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name()));
    configuration.setAllowedHeaders(InfraestructureConstant.ALLOWED_HEADERS);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    
    String[] permitidos = configuracionSeguridadProperties.dominios().getPermitidosArray();
    log.info("Servicio denominado prueba-ws y tiene los siguientes dominios permitidos {} => {}",
        permitidos.length, Arrays.toString(permitidos));
    
    return source;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers(
        headers -> headers
        .frameOptions(FrameOptionsConfig::deny)
        .httpStrictTransportSecurity(
            hsts -> hsts.includeSubDomains(true)
            .maxAgeInSeconds(31536000))
        .contentTypeOptions(Customizer.withDefaults())
        )
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/healthcheck").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(new AuditValidationFilter(configuracionSeguridadProperties),
            JwtAuthorizationFilter.class)
        .addFilter(new JwtLoginAuthenticationFilter(authenticationManager(),
            autenticarClienteUseCasePort, configuracionSeguridadProperties, encryptUtils))
        .addFilter(new JwtAuthorizationFilter(authenticationManager(),
            validarAutorizacionUseCaseAdapter, configuracionSeguridadProperties))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 12) {
      @Override
      public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
          throw new IllegalArgumentException("rawPassword cannot be null");
        }
        String password = rawPassword.toString();
        if (password.getBytes().length > 72) {
          log.warn("Password truncated to 72 bytes due to BCrypt limitation");
          byte[] passwordBytes = password.getBytes();
          password = new String(passwordBytes, 0, 72);
          while (!password.isEmpty() && password.getBytes().length > 72) {
            password = password.substring(0, password.length() - 1);
          }
        }
        return super.encode(password);
      }
    };
  }

  @Bean
  @Primary
  DaoAuthenticationProvider authenticationProvider() {
    UserDetailsServiceAdapter userDetailsService =
        new UserDetailsServiceAdapter(obtenerInfoClienteUseCasePort, passwordEncoder());
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  AuthenticationManager authenticationManager() {
    return new ProviderManager(authenticationProvider());
  }

  @Bean
  public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("/publico/**");
  }
}
