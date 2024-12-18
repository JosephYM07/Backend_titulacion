package com.tesis.tigmotors.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tesis.tigmotors.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Configuración principal para la autenticación y autorización del sistema.
 *
 * Define y expone los beans relacionados con:
 * - Gestión de autenticación.
 * - Proveedor de autenticación.
 * - Cifrado de contraseñas.
 * - Carga de detalles de usuario desde la base de datos.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Configura y expone el `AuthenticationManager`, que es el componente central
     * encargado de manejar la autenticación en Spring Security.
     *
     * @param config La configuración de autenticación proporcionada por Spring Security.
     * @return Una instancia de {@link AuthenticationManager}.
     * @throws Exception Si ocurre algún error al inicializar el `AuthenticationManager`.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura y expone un proveedor de autenticación basado en DAO, que utiliza
     * un servicio de detalles de usuario y un codificador de contraseñas para validar
     * las credenciales de los usuarios.
     *
     * @return Una instancia de {@link AuthenticationProvider}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    /**
     * Configura y expone un codificador de contraseñas basado en el algoritmo BCrypt.
     * Este codificador se utiliza para cifrar y comparar contraseñas de manera segura.
     *
     * @return Una instancia de {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura y expone un servicio de detalles de usuario que se encarga de cargar
     * los datos de un usuario desde la base de datos mediante el `UserRepository`.
     *
     * @return Una instancia de {@link UserDetailsService}.
     * @throws UsernameNotFoundException Si el usuario no se encuentra en la base de datos.
     */
    @Bean
    public UserDetailsService userDetailService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("models not found"));
    }

}
