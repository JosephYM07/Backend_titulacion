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

@Configuration
public class AdminUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    //Centro de servicios
    @Value("${serviceStaff.username}")
    private String serviceStaffUsername;

    @Value("${serviceStaff.password}")
    private String serviceStaffPassword;

    @Value("${serviceStaff.email}")
    private String serviceStaffEmail;

    @Bean
    CommandLineRunner createAdminUser() {
        return args -> {
            try {

                if (userRepository.findByUsername(adminUsername).isEmpty()) {
                    // Crear un usuario administrador
                    User adminUser = User.builder()
                            .username(adminUsername)
                            .password(passwordEncoder.encode(adminPassword))
                            .email(adminEmail)
                            .role(Role.ADMIN)
                            .permiso(true)
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
                            .password(passwordEncoder.encode(serviceStaffPassword))
                            .email(serviceStaffEmail)
                            .role(Role.PERSONAL_CENTRO_DE_SERVICIOS)
                            .permiso(true)
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