package com.tesis.tigmotors.Config;

import com.tesis.tigmotors.Jwt.JwtAuthenticationFilter;
import com.tesis.tigmotors.security.CustomAccessDeniedHandler;
import com.tesis.tigmotors.security.CustomNotFoundHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomNotFoundHandler customNotFoundHandler;

    @Value("${url.backend}")
    private String urlBackend;

    @Value("${url.frontend}")
    private String urlFrontend;

    /**
     * Configura la cadena de filtros de seguridad para la aplicación.
     *
     * - Deshabilita CSRF ya que se utiliza un esquema basado en tokens JWT.
     * - Configura reglas globales de CORS para permitir acceso controlado desde el frontend.
     * - Define políticas de acceso según roles y privilegios.
     * - Implementa un esquema de sesiones sin estado para las solicitudes (STATELESS).
     * - Agrega el filtro de autenticación JWT para validar las solicitudes.
     * - Define un manejador personalizado para accesos denegados.
     *
     * @param http Objeto {@link HttpSecurity} que permite configurar la seguridad HTTP.
     * @return Una instancia de {@link SecurityFilterChain}.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configuración de CORS global
                .authorizeHttpRequests(authRequest -> authRequest
                        .requestMatchers("/api/v1/**","/api/public").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api-user/**","/crear-solicitud").hasAuthority("USER")
                        .requestMatchers("/service-staff/**").hasAuthority("PERSONAL_CENTRO_DE_SERVICIOS")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/api/v1/login-global"))
                // Configuración de sesiones sin estado
                .sessionManagement(sessionManager -> sessionManager
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Agregar el filtro JWT antes del filtro estándar de autenticación
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Manejador de accesos denegados personalizado
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(customNotFoundHandler))

                // Construir la configuración
                .build();
    }

    /**
     * Configuración global de CORS para permitir interacciones seguras entre frontend y backend.
     *
     * - Define los orígenes permitidos para realizar solicitudes al backend.
     * - Configura los métodos HTTP permitidos.
     * - Especifica los encabezados que pueden enviarse con las solicitudes.
     * - Habilita el uso de credenciales (si es necesario).
     *
     * @return Una instancia de {@link CorsConfigurationSource}.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitir solo el origen del frontend
/*
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173/"));
*/
        config.setAllowedOrigins(Arrays.asList(urlBackend, urlFrontend));
        // Permitir los métodos HTTP necesarios
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permitir los encabezados necesarios
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin"));
        // Permitir credenciales, importante para cookies
        config.setAllowCredentials(true);
        // Registrar la configuración para todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
