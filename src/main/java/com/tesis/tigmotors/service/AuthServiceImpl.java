package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.AuthExceptions;
import com.tesis.tigmotors.dto.Response.AuthResponse;
import com.tesis.tigmotors.dto.Request.LoginRequest;
import com.tesis.tigmotors.dto.Request.RegisterRequest;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final JwtServiceImpl jwtServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailServiceImpl emailServiceImpl;

    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        try {
            // Autenticar al usuario con nombre de usuario y contraseña
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Buscar al usuario en la base de datos
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AuthExceptions.UserNotFoundException("Usuario no encontrado"));

            // Validar que el usuario esté aprobado
            if (!user.isPermiso()) {
                log.warn("Usuario no aprobado: " + user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder()
                        .status("Error")
                        .message("Su cuenta aún no ha sido aprobada por el administrador")
                        .build());
            }

            // Generar el token JWT para el usuario autenticado
            String accessToken = jwtServiceImpl.generateAccessToken(user);
            return ResponseEntity.ok(AuthResponse.builder()
                    .status("success")
                    .message("Autenticación exitosa")
                    .token(accessToken)
                    .build());
        } catch (AuthenticationException e) {
            // Manejar error de autenticación
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder()
                    .status("Error")
                    .message("Nombre de usuario o contraseña no válidos")
                    .build());
        }
    }

    //Registro publico de usuarios
    public ResponseEntity<AuthResponse> register(RegisterRequest request) {
        try {
            // Verificar si el nombre de usuario ya está en uso en la base de datos y correo electrónico
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponse.builder()
                        .status("Error")
                        .message("El nombre de usuario ya está en uso")
                        .build());
            }
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponse.builder()
                        .status("Error")
                        .message("El correo electrónico ya está en uso")
                        .build());
            }
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .business_name(request.getBusiness_name())
                    .email(request.getEmail())
                    .phone_number(request.getPhone_number())
                    .role(Role.USER)
                    .permiso(false)
                    .build();

            // Guardar el usuario
            User savedUser = userRepository.save(user);
            log.info("Usuario guardado con ID: {}", savedUser.getId());

            // Crear la secuencia asociada al usuario
            userRepository.guardarSecuencia(savedUser.getId());
            log.info("Secuencia creada para el usuario con ID: {}", savedUser.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                    .message("Registro exitoso, espere a que el administrador apruebe su cuenta para poder iniciar sesión")
                    .build());
        } catch (Exception e) {
            log.error("Error al registrar usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthResponse.builder()
                    .status("Error")
                    .message("Error al registrar usuario")
                    .build());
        }
    }
    //Registro de usuarios por parte del administrador
    public ResponseEntity<AuthResponse> registerByAdmin(RegisterRequest request, String adminUsername) {
        try {
            // Verificar si el administrador tiene permisos para crear usuarios
            User adminUser = userRepository.findByUsername(adminUsername)
                    .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

            if (!adminUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthResponse.builder()
                        .status("Error")
                        .message("No tiene permisos para registrar usuarios")
                        .build());
            }

            // Validar si el nombre de usuario ya está en uso
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponse.builder()
                        .status("Error")
                        .message("El nombre de usuario ya está en uso")
                        .build());
            }

            // Validar si el correo ya está en uso
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponse.builder()
                        .status("Error")
                        .message("El correo electrónico ya está en uso")
                        .build());
            }

            // Crear el usuario
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .business_name(request.getBusiness_name())
                    .email(request.getEmail())
                    .phone_number(request.getPhone_number())
                    .role(Role.USER)
                    .permiso(true)
                    .build();

            // Guardar el usuario
            User savedUser = userRepository.save(user);
            log.info("Usuario creado por el administrador con ID: {}", savedUser.getId());

            // Crear la secuencia asociada al usuario
            userRepository.guardarSecuencia(savedUser.getId());
            log.info("Secuencia creada para el usuario con ID: {}", savedUser.getId());

            // Enviar notificación por correo electrónico
            String to = user.getEmail();
            String subject = "Tu cuenta ha sido creada - TigMotors";
            String content = buildAccountCreatedByAdminEmailContent(user.getUsername());

            try {
                emailServiceImpl.sendEmail(to, subject, content);
                log.info("Correo de creación de cuenta enviado a: {}", to);
            } catch (RuntimeException e) {
                log.error("Error al enviar correo de creación de cuenta: {}", e.getMessage());
            }

            // Retornar la respuesta
            return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                    .message("Usuario creado exitosamente por el administrador ya puedes acceder a la plataforma!")
                    .build());
        } catch (Exception e) {
            log.error("Error al registrar usuario por administrador: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthResponse.builder()
                    .status("Error")
                    .message("Error al registrar usuario por administrador")
                    .build());
        }
    }

    private String buildAccountCreatedByAdminEmailContent(String username) {
        return "<html>" +
                "<meta charset='UTF-8'>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<div style='text-align: center;'>" +
                "<img src='https://yourcompany.com/logo.png' alt='TigMotors Logo' style='width: 150px; margin-bottom: 20px;' />" +
                "</div>" +
                "<h2 style='color: #333;'>¡Tu cuenta ha sido creada!</h2>" +
                "<p>Hola " + username + ",</p>" +
                "<p>Nos complace informarte que un administrador de TigMotors ha creado tu cuenta exitosamente.</p>" +
                "<p>Puedes acceder a nuestra plataforma utilizando tus credenciales y disfrutar de todos los servicios que TigMotors tiene para ofrecerte.</p>" +
                "<p>Por favor, <a href='https://yourcompany.com/login' style='color: #4CAF50;'>haz clic aquí</a> para iniciar sesión en tu cuenta.</p>" +
                "<br>" +
                "<p>Gracias por confiar en TigMotors.</p>" +
                "<br>" +
                "<p>Atentamente,</p>" +
                "<p>El equipo de TigMotors</p>" +
                "<div style='text-align: center; font-size: 12px; color: #888; margin-top: 20px;'>" +
                "<p>TigMotors © 2024 | Todos los derechos reservados</p>" +
                "<p><a href='https://yourcompany.com/terms' style='color: #888;'>Términos y Condiciones</a> | <a href='https://yourcompany.com/privacy' style='color: #888;'>Política de Privacidad</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

}
