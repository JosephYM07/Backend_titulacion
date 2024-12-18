package com.tesis.tigmotors.Config;

import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración inicial para la creación de usuarios predeterminados en el sistema,
 * como el administrador y el personal del centro de servicios.
 */
@Configuration
public class AdminUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Variables para el usuario administrador
    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.business_name}")
    private String adminBusinessName;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.phoneNumber}")
    private String adminPhoneNumber;

    @Value("${admin.permiso}")
    private boolean adminPermiso;

    // Variables para el usuario del centro de servicios
    @Value("${serviceStaff.username}")
    private String serviceStaffUsername;

    @Value("${serviceStaff.password}")
    private String serviceStaffPassword;

    @Value("${serviceStaff.email}")
    private String serviceStaffEmail;

    @Value("${serviceStaff.business_name}")
    private String serviceStaffBusinessName;

    @Value("${serviceStaff.phoneNumber}")
    private String serviceStaffPhoneNumber;

    @Value("${serviceStaff.permiso}")
    private boolean serviceStaffPermiso;

    /**
     * Inicializa los usuarios predeterminados en el sistema al arrancar la aplicación.
     *
     * - Crea un usuario administrador si no existe en la base de datos.
     * - Crea un usuario del centro de servicios si no existe en la base de datos.
     *
     * @return Un {@link CommandLineRunner} que ejecuta la creación de usuarios.
     */
    @Bean
    CommandLineRunner createAdminUser() {
        return args -> {
            try {

                if (userRepository.findByUsername(adminUsername).isEmpty()) {
                    // Crear un usuario administrador
                    User adminUser = User.builder()
                            .username(adminUsername)
                            .business_name(adminBusinessName)
                            .password(passwordEncoder.encode(adminPassword))
                            .email(adminEmail)
                            .phone_number(adminPhoneNumber)
                            .role(Role.ADMIN)
                            .permiso(adminPermiso)
                            .build();

                    userRepository.save(adminUser);
                    log.info("Usuario administrador creado con éxito");
                } else {
                    log.info("El usuario administrador ya existe");
                }
                if (userRepository.findByUsername(serviceStaffUsername).isEmpty()) {
                    // Crear un usuario Staff Centro de servivios
                    User serviceStaff = User.builder()
                            .username(serviceStaffUsername)
                            .business_name(serviceStaffBusinessName)
                            .password(passwordEncoder.encode(serviceStaffPassword))
                            .email(serviceStaffEmail)
                            .phone_number(serviceStaffPhoneNumber)
                            .role(Role.PERSONAL_CENTRO_DE_SERVICIOS)
                            .permiso(serviceStaffPermiso)
                            .build();
                    userRepository.save(serviceStaff);
                    log.info("Usuario Personal Centro de Servicios creado con éxito");
                } else {
                    log.info("Usuario Personal Centro de Servicios ya existe");
                }
            } catch (Exception e) {
                log.error("Usuario Personal Centro de Servicios creado con éxito");
                e.printStackTrace();
            }
        };
    }
}