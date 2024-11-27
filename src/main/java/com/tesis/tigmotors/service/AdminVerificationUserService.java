package com.tesis.tigmotors.service;


import com.tesis.tigmotors.dto.Request.PendingUserDTO;
import com.tesis.tigmotors.dto.Response.ErrorResponse;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.PasswordResetTokenRepository;
import com.tesis.tigmotors.repository.RefreshTokenRepository;
import com.tesis.tigmotors.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminVerificationUserService {

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public ResponseEntity<Object> getUsersStatus() {
        try {
            long pendingUsersCount = userRepository.countByPermisoAndRole(false, Role.USER);
            long approvedUsersCount = userRepository.countByPermisoAndRole(true, Role.USER);

            Map<String, Long> response = Map.of(
                    "Pendiente", pendingUsersCount,
                    "Por Aprobar", approvedUsersCount
            );

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error al obtener el estado de los usuarios: {}", ex.getMessage(), ex);
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error al obtener el estado de los usuarios"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public ResponseEntity<Object> getPendingUsers() {
        try {
            List<User> pendingUsers = userRepository.findByPermiso(false);

            if (pendingUsers.isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "No hay usuarios pendientes de aprobación");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            List<PendingUserDTO> pendingUsersDTOs = pendingUsers.stream()
                    .map(user -> new PendingUserDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getBusiness_name(),
                            user.getPhone_number(),
                            user.getRole(),
                            user.getEmail(),
                            user.isPermiso()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(pendingUsersDTOs);
        } catch (Exception ex) {
            log.error("Error al obtener usuarios pendientes", ex);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al obtener los usuarios pendientes");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Método para aprobar un usuario
    public ResponseEntity<Object> approveUser(Integer userId) {
        try {
            // Buscar el usuario por ID
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty()) {
                // Usuario no encontrado
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Usuario no encontrado"));
            }

            User user = userOptional.get();

            // Validar si ya está aprobado
            if (user.isPermiso()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "El usuario ya ha sido aprobado previamente"));
            }

            // Aprobar al usuario
            user.setPermiso(true);
            userRepository.save(user);

            // Enviar notificación por correo electrónico
            String to = user.getEmail();
            String subject = "Aprobación de Cuenta - TigMotors";
            String content = buildAccountApprovalEmailContent(user.getUsername());

            try {
                emailService.sendEmail(to, subject, content);
            } catch (RuntimeException e) {
                System.err.println("Error al enviar correo de aprobación: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("message", "Usuario aprobado con éxito"));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al aprobar el usuario"));
        }
    }

    // Método para eliminar un usuario por ID
    public ResponseEntity<Object> deleteUserById(Integer userId) {
        try {
            // Buscar el usuario por ID
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty()) {
                // Usuario no encontrado
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Usuario no encontrado"));
            }

            User user = userOptional.get();
            if (!user.getRole().equals(Role.USER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "No tienes permiso para eliminar este usuario"));
            }
            passwordResetTokenRepository.deleteByUserId(userId);
            refreshTokenRepository.deleteByUserId(userId);
            userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "Usuario eliminado con éxito"));

        } catch (Exception ex) {
            log.error("Error al eliminar el usuario con ID {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al eliminar el usuario"));
        }
    }


    private String buildAccountApprovalEmailContent(String username) {
        return "<html>" +
                "<meta charset='UTF-8'>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<div style='text-align: center;'>" +
                "<img src='https://yourcompany.com/logo.png' alt='TigMotors Logo' style='width: 150px; margin-bottom: 20px;' />" +
                "</div>" +
                "<h2 style='color: #333;'>¡Tu cuenta ha sido aprobada!</h2>" +
                "<p>Hola " + username + ",</p>" +
                "<p>Nos complace informarte que tu cuenta ha sido aprobada exitosamente. Ahora puedes acceder a nuestra plataforma y disfrutar de todos los servicios que TigMotors tiene para ofrecerte.</p>" +
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
