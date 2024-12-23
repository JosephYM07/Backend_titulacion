package com.tesis.tigmotors.service;


import com.tesis.tigmotors.Exceptions.InvalidRequestException;
import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.dto.Request.PendingUserDTO;
import com.tesis.tigmotors.dto.Response.ErrorResponse;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.PasswordResetTokenRepository;
import com.tesis.tigmotors.repository.RefreshTokenRepository;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.AdminVerificationUserService;
import com.tesis.tigmotors.service.interfaces.EmailService;
import com.tesis.tigmotors.utils.RoleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminVerificationUserServiceImpl implements AdminVerificationUserService {

    private final UserRepository userRepository;

    private final EmailService emailServiceImpl;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final RoleValidator roleValidator;

    @Value("${url.frontend.login}")
    private String urlLogin;

    /**
     * Obtiene el estado de los usuarios (pendientes y aprobados).
     *
     * @return ResponseEntity con el conteo de usuarios pendientes y aprobados.
     */
    @Override
    @Transactional
    public ResponseEntity<Object> getUsersStatus() {
        try {
            // Obtener el conteo de usuarios pendientes y aprobados
            long pendingUsersCount = userRepository.countByPermisoAndRole(false, Role.USER);
            long approvedUsersCount = userRepository.countByPermisoAndRole(true, Role.USER);

            // Validar que las consultas hayan devuelto datos consistentes
            if (pendingUsersCount < 0 || approvedUsersCount < 0) {
                throw new InvalidRequestException("Los datos del estado de los usuarios no son consistentes.");
            }

            // Crear la respuesta
            Map<String, Long> response = Map.of(
                    "Pendiente", pendingUsersCount,
                    "Aprobado", approvedUsersCount
            );

            return ResponseEntity.ok(response);

        } catch (InvalidRequestException ex) {
            log.error("Error de validación al obtener el estado de los usuarios: {}", ex.getMessage(), ex);
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (Exception ex) {
            log.error("Error inesperado al obtener el estado de los usuarios: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al obtener el estado de los usuarios.", ex); // Manejado globalmente
        }
    }

    /**
     * Obtiene una lista de usernames de usuarios aprobados.
     *
     * @param authentication el objeto de autenticación del usuario.
     * @return Lista de usernames de usuarios aprobados.
     */

    @Override
    @Transactional
    public List<String> obtenerUsernamesAprobados(Authentication authentication) {

        if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
            throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
        }

        try {
            List<User> usuariosAprobados = userRepository.findByPermisoAndRole(true, Role.USER);
            if (usuariosAprobados.isEmpty()) {
                throw new ResourceNotFoundException("No hay usuarios aprobados con rol USER.");
            }
            return usuariosAprobados.stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException ex) {
            log.error("No hay usuarios aprobados con rol USER: {}", ex.getMessage());
            throw ex;
        } catch (SecurityException ex) {
            log.error("Acceso denegado: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado al obtener usernames aprobados: {}", ex.getMessage());
            throw new RuntimeException("Error inesperado al obtener usernames aprobados.", ex);
        }
    }

    /**
     * Obtiene una lista de usuarios aprobados con el rol USER.
     *
     * @param authentication el objeto de autenticación del usuario.
     * @return Lista de PendingUserDTO que representan a los usuarios aprobados.
     */
    @Override
    @Transactional
    public List<PendingUserDTO> obtenerUsuariosAprobados(Authentication authentication) {

        if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
            throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
        }
        try {
            // Obtener usuarios aprobados con rol USER
            List<User> usuariosAprobados = userRepository.findByPermisoAndRole(true, Role.USER);
            if (usuariosAprobados.isEmpty()) {
                throw new ResourceNotFoundException("No hay usuarios aprobados con rol USER.");
            }
            // Convertir usuarios aprobados a DTO
            return usuariosAprobados.stream()
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
        } catch (ResourceNotFoundException ex) {
            log.error("No hay usuarios aprobados con rol USER: {}", ex.getMessage(), ex);
            throw ex; // Delegado al GlobalExceptionHandler
        } catch (Exception ex) {
            log.error("Error inesperado al obtener usuarios aprobados: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al obtener usuarios aprobados.", ex); // Delegado globalmente
        }
    }

    /**
     * Obtiene una lista de usuarios pendientes de aprobación.
     *
     * @return ResponseEntity con la lista de usuarios pendientes o un error.
     */
    @Override
    @Transactional
    public ResponseEntity<Object> getPendingUsers() {
        try {
            List<User> pendingUsers = userRepository.findByPermiso(false);
            if (pendingUsers.isEmpty()) {
                throw new ResourceNotFoundException("No hay usuarios pendientes de aprobación.");
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
        } catch (ResourceNotFoundException ex) {
            log.error("No hay usuarios pendientes de aprobación.", ex);
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (Exception ex) {
            log.error("Error inesperado al obtener usuarios pendientes", ex);
            throw new RuntimeException("Error inesperado al obtener usuarios pendientes.", ex); // Manejado globalmente
        }
    }

    /**
     * Aprueba un usuario dado su ID.
     *
     * @param userId ID del usuario a aprobar.
     * @return ResponseEntity con el resultado de la operación.
     */
    @Override
    @Transactional
    public ResponseEntity<Object> approveUser(Integer userId) {
        try {
            // Buscar el usuario por ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID '" + userId + "' no encontrado."));

            // Validar si ya está aprobado
            if (user.isPermiso()) {
                throw new IllegalStateException("El usuario ya ha sido aprobado previamente.");
            }

            // Aprobar al usuario
            user.setPermiso(true);
            userRepository.save(user);

            // Enviar notificación por correo electrónico
            try {
                String to = user.getEmail();
                String subject = "Aprobación de Cuenta - TigMotors";
                String content = buildAccountApprovalEmailContent(user.getUsername());
                emailServiceImpl.sendEmail(to, subject, content);
            } catch (RuntimeException e) {
                log.error("Error al enviar correo de aprobación para el usuario con ID {}: {}", userId, e.getMessage(), e);
            }

            // Retornar respuesta exitosa
            return ResponseEntity.ok(Map.of("message", "Usuario aprobado con éxito"));

        } catch (ResourceNotFoundException ex) {
            log.error("Usuario no encontrado con ID {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        } catch (IllegalStateException ex) {
            log.error("Error de estado: {}", ex.getMessage(), ex);
            throw ex;
        } catch (InvalidRequestException ex) {
            log.error("Solicitud inválida para aprobar usuario: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado al aprobar el usuario con ID {}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al aprobar el usuario.", ex); // Manejado globalmente
        }
    }


    /**
     * Elimina un usuario dado su ID.
     *
     * @param userId ID del usuario a eliminar.
     * @return ResponseEntity con el resultado de la operación.
     */
    @Override
    @Transactional
    public ResponseEntity<Object> deleteUserById(Integer userId) {
        try {
            // Validar que el ID del usuario sea válido
            if (userId == null || userId <= 0) {
                throw new InvalidRequestException("El ID del usuario es obligatorio y debe ser mayor que 0.");
            }

            // Buscar el usuario por ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID '" + userId + "' no encontrado."));

            // Validar que el usuario tenga el rol USER
            if (!user.getRole().equals(Role.USER)) {
                throw new SecurityException("No tienes permiso para eliminar este usuario.");
            }

            // Eliminar tokens asociados al usuario
            passwordResetTokenRepository.deleteByUserId(userId);

            // Eliminar al usuario
            userRepository.delete(user);

            // Retornar respuesta exitosa
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado con éxito"));

        } catch (ResourceNotFoundException ex) {
            log.error("Usuario no encontrado con ID {}: {}", userId, ex.getMessage(), ex);
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (SecurityException ex) {
            log.error("Acceso denegado para eliminar el usuario con ID {}: {}", userId, ex.getMessage(), ex);
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (InvalidRequestException ex) {
            log.error("Solicitud inválida para eliminar usuario: {}", ex.getMessage(), ex);
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (Exception ex) {
            log.error("Error inesperado al eliminar el usuario con ID {}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al eliminar el usuario.", ex); // Manejado globalmente
        }
    }

    /**
     * Construye el contenido del correo electrónico de aprobación de cuenta.
     *
     * @param username el nombre de usuario del destinatario.
     * @return el contenido del correo electrónico.
     */
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
                "<p>Por favor, <a href='\" + urlLogin + \"' style='color: #4CAF50;'>haz clic aquí</a> para iniciar sesión en tu cuenta.</p>" +
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
