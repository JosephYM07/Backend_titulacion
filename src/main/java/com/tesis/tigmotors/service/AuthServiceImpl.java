package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.AuthExceptions;
import com.tesis.tigmotors.Exceptions.InvalidRequestException;
import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final JwtServiceImpl jwtServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailServiceImpl emailServiceImpl;

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.phoneNumber}")
    private String adminPhoneNumber;

    @Value("${url.frontend.login}")
    private String urlFrontendLogin;


    @Override
    @Transactional
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        try {
            // Validar que los datos de entrada no estén vacíos
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new InvalidRequestException("El nombre de usuario es obligatorio.");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new InvalidRequestException("La contraseña es obligatoria.");
            }

            // Autenticar al usuario con nombre de usuario y contraseña
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Buscar al usuario en la base de datos
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            // Validar que el usuario esté aprobado
            if (!user.isPermiso()) {
                log.warn("Usuario no aprobado: {}", user.getUsername());
                throw new AccessDeniedException("Su cuenta aún no ha sido aprobada por el administrador.");
            }

            // Generar el token JWT para el usuario autenticado
            String accessToken = jwtServiceImpl.generateAccessToken(user);
            // Enviar notificación por correo electrónico
            if (user.getRole().equals(Role.USER)) {
                String emailContent = construirCorreoInicioSesion(user.getUsername());
                emailServiceImpl.sendEmail(user.getEmail(), "Inicio de sesión en TigMotors", emailContent);
            }
            // Respuesta exitosa
            return ResponseEntity.ok(AuthResponse.builder()
                    .status("success")
                    .message("Autenticación exitosa")
                    .token(accessToken)
                    .build());

        } catch (ResourceNotFoundException ex) {
            // Log específico para el caso de usuario no encontrado
            log.error("Error: Usuario no encontrado - {}", ex.getMessage(), ex);
            throw ex; // Relanzar la excepción sin modificar el mensaje
        } catch (AuthenticationException ex) {
            // Log específico para errores de autenticación
            log.error("Error de autenticación para el usuario: {}", request.getUsername(), ex);
            throw new InvalidRequestException("Nombre de usuario o contraseña no válidos.");
        } catch (AccessDeniedException ex) {
            // Log específico para errores de acceso denegado
            log.warn("Error de acceso denegado para el usuario: {}", request.getUsername(), ex);
            throw ex; // Relanzar la excepción sin modificar el mensaje
        } catch (Exception ex) {
            // Manejo genérico para otras excepciones
            log.error("Error inesperado durante el inicio de sesión: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado durante el inicio de sesión.", ex);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, String>> register(RegisterRequest request) {
        try {
            // Validar datos de entrada
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new InvalidRequestException("El nombre de usuario es obligatorio.");
            }
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new InvalidRequestException("El correo electrónico es obligatorio.");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new InvalidRequestException("La contraseña es obligatoria.");
            }

            // Verificar si el nombre de usuario o correo ya existen
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new InvalidRequestException("El nombre de usuario ya está en uso.");
            }
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new InvalidRequestException("El correo electrónico ya está en uso.");
            }

            // Crear y guardar el usuario
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .business_name(request.getBusiness_name())
                    .email(request.getEmail())
                    .phone_number(request.getPhone_number())
                    .role(Role.USER)
                    .permiso(false)
                    .build();

            userRepository.save(user);
            log.info("Usuario registrado con éxito: {}", user.getUsername());
            // Enviar correo de confirmación
            String emailContent = construirCorreoRegistro(request.getUsername(), request.getEmail());
            emailServiceImpl.sendEmail(request.getEmail(), "Registro en TigMotors", emailContent);

            // Retornar solo el mensaje
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registro exitoso, espere a que el administrador apruebe su cuenta para poder iniciar sesión.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InvalidRequestException ex) {
            log.error("Error de validación al registrar usuario: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado al registrar usuario: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al registrar usuario.", ex);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, String>> registerByAdmin(RegisterRequest request, String adminUsername) {
        try {
            // Validar que el administrador tenga permisos para registrar usuarios
            User adminUser = userRepository.findByUsername(adminUsername)
                    .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado"));

            if (!adminUser.getRole().equals(Role.ADMIN)) {
                throw new AccessDeniedException("No tiene permisos para registrar usuarios.");
            }

            // Validar datos del usuario a registrar
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new InvalidRequestException("El nombre de usuario es obligatorio.");
            }
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new InvalidRequestException("El correo electrónico es obligatorio.");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new InvalidRequestException("La contraseña es obligatoria.");
            }

            // Verificar si el nombre de usuario ya está en uso
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new InvalidRequestException("El nombre de usuario ya está en uso.");
            }

            // Verificar si el correo ya está en uso
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new InvalidRequestException("El correo electrónico ya está en uso.");
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
            try {
                String to = user.getEmail();
                String subject = "Tu cuenta ha sido creada - TigMotors";
                String content = buildAccountCreatedByAdminEmailContent(user.getUsername());
                emailServiceImpl.sendEmail(to, subject, content);
                log.info("Correo de creación de cuenta enviado a: {}", to);
            } catch (RuntimeException e) {
                log.error("Error al enviar correo de creación de cuenta: {}", e.getMessage());
            }

            // Crear y devolver la respuesta
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario creado exitosamente por el administrador. ¡Ya puedes acceder a la plataforma!");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResourceNotFoundException | AccessDeniedException | InvalidRequestException ex) {
            log.error("Error en el proceso de registro por el administrador: {}", ex.getMessage(), ex);
            throw ex; // Delegado al GlobalExceptionHandler
        } catch (Exception ex) {
            log.error("Error inesperado al registrar usuario por administrador: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al registrar usuario por administrador.", ex); // Delegado globalmente
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, String>> logout(String authHeader, String username) {

        try {
            // Validar el encabezado Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidRequestException("Token no proporcionado o formato inválido.");
            }

            // Extraer el token del encabezado
            String token = authHeader.substring(7); // Remover "Bearer "

            // Verificar que el token pertenece al usuario actual
            String tokenUsername = jwtServiceImpl.getUsernameFromToken(token);
            if (!tokenUsername.equals(username)) {
                log.error("El token no pertenece al usuario autenticado: {}", username);
                throw new AccessDeniedException("No tienes permiso para cerrar sesión con este token.");
            }

            // Invalidar el token
            jwtServiceImpl.invalidateToken(token);

            // Respuesta de éxito
            Map<String, String> response = new HashMap<>();
            response.put("message", "Sesión cerrada correctamente para el usuario: " + username);

            log.info("Sesión cerrada correctamente para el usuario: {}", username);
            return ResponseEntity.ok(response);

        } catch (InvalidRequestException | AccessDeniedException ex) {
            log.error("Error durante el cierre de sesión: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado durante el cierre de sesión: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado durante el cierre de sesión.", ex);
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
                "<p>Por favor, <a href='" + urlFrontendLogin + "' style='color: #4CAF50;'>haz clic aquí</a> para iniciar sesión en tu cuenta.</p>" +
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

    private String construirCorreoRegistro(String username, String email) {
        return "<html>" +
                "<meta charset='UTF-8'>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<h2 style='color: #333;'>Bienvenido a TigMotors, " + username + "</h2>" +
                "<p>Hola " + username + ",</p>" +
                "<p>Tu cuenta ha sido registrada con éxito, pero necesitas esperar a que un administrador apruebe tu cuenta antes de poder iniciar sesión.</p>" +
                "<p>Si necesitas más información o si tu cuenta no es aprobada en un tiempo razonable, por favor contacta a nuestro equipo de soporte:</p>" +
                "<ul>" +
                "<li><strong>Correo:</strong> " + adminEmail + "</li>" +
                "<li><strong>Teléfono:</strong> " + adminPhoneNumber + "</li>" +
                "</ul>" +
                "<p>Gracias por registrarte en TigMotors. Estamos aquí para ayudarte en todo lo que necesites.</p>" +
                "<br>" +
                "<p>Atentamente,</p>" +
                "<p>El equipo de TigMotors</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String construirCorreoInicioSesion(String username) {
        String fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return "<html>" +
                "<meta charset='UTF-8'>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<h2 style='color: #333;'>Inicio de sesión detectado</h2>" +
                "<p>Hola " + username + ",</p>" +
                "<p>Se ha detectado un inicio de sesión en tu cuenta de TigMotors.</p>" +
                "<p>Fecha y hora: " + fechaHora + "</p>" +
                "<p>Si no reconoces esta actividad, por favor contacta de inmediato a nuestro equipo de soporte.</p>" +
                "<br>" +
                "<p>Atentamente,</p>" +
                "<p>El equipo de TigMotors</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


}
