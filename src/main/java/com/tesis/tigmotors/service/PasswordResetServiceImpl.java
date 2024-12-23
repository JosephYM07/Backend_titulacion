package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.AuthExceptions;
import com.tesis.tigmotors.Exceptions.UnauthorizedOperationException;
import com.tesis.tigmotors.dto.Response.ErrorResponse;
import com.tesis.tigmotors.models.PasswordResetToken;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.PasswordResetTokenRepository;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.JwtService;
import com.tesis.tigmotors.service.interfaces.PasswordResetService;
import com.tesis.tigmotors.service.interfaces.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.tesis.tigmotors.enums.Role;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private static final long TOKEN_EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${url.frontend.cambiar.contrasenia}")
    private String urlCambiarContrasenia;


    @Transactional
    public String sendResetToken(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthExceptions.UserNotFoundException("Usuario no encontrado con ese correo"));

            if (!user.getRole().equals(Role.USER)) {
                throw new UnauthorizedOperationException("Solo los usuarios con rol USER pueden solicitar el restablecimiento de contraseña.");
            }

            log.info("Eliminando cualquier token anterior del usuario: {}", user.getEmail());
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            Instant expiryDate = Instant.now().plus(TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES);

            log.info("Generando nuevo token para el usuario: {}", user.getEmail());

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .token(token)
                    .expiryDate(expiryDate)
                    .user(user)
                    .build();
            passwordResetTokenRepository.save(passwordResetToken);

            String subject = "Recuperación de contraseña - TigMotors";
            String content = buildResetPasswordEmailContent(token);
            emailService.sendEmail(email, subject, content); // Usando la interfaz

            log.info("Código de recuperación enviado exitosamente al correo: {}", email);
            return "Código de recuperación enviado al correo";
        } catch (AuthExceptions.UserNotFoundException e) {
            log.error("Usuario no encontrado con ese correo: {}", email);
            throw e;
        } catch (UnauthorizedOperationException e) {
            log.error("Operación no autorizada para el correo: {}", email);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al enviar el token de restablecimiento: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el token de restablecimiento");
        }
    }

    /**
     * Restablece la contraseña del usuario usando un token de recuperación.
     *
     * @param token       Token de recuperación enviado al correo del usuario.
     * @param newPassword Nueva contraseña que se establecerá para el usuario.
     * @return Mensaje de éxito si la contraseña se restablece correctamente.
     */
    @Override
    @Transactional
    public String resetPassword(String token, String newPassword) {
        try {
            log.info("Buscando el token en la base de datos: {}", token);

            // Buscar el token en la base de datos
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token inválido o caducado"));

            log.info("Token encontrado: {}", resetToken);

            // Verificar si el token ha expirado
            if (resetToken.getExpiryDate().isBefore(Instant.now())) {
                log.error("El token ha caducado: {}", resetToken);
                throw new RuntimeException("El token ha caducado");
            }

            // Obtener el usuario asociado al token
            User user = resetToken.getUser();
            log.info("Restableciendo contraseña para el usuario: {}", user.getEmail());

            // Verificar que la nueva contraseña no sea igual a la actual
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                log.error("La nueva contraseña no puede ser igual a la contraseña actual para el usuario: {}", user.getEmail());
                throw new RuntimeException("La nueva contraseña no puede ser igual a la contraseña actual");
            }

            // Codificar y guardar la nueva contraseña
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Eliminar el token después de usarlo
            passwordResetTokenRepository.delete(resetToken);
            log.info("Token eliminado después de restablecer la contraseña: {}", token);

            // Enviar notificación al usuario
            sendPasswordChangeNotification(user.getEmail());

            return "Contraseña actualizada correctamente";
        } catch (RuntimeException e) {
            log.error("Error al restablecer la contraseña: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al restablecer la contraseña: {}", e.getMessage());
            throw new RuntimeException("Error al restablecer la contraseña");
        }
    }

    /**
     * Cambia la contraseña de un usuario autenticado.
     *
     * @param username        Nombre de usuario autenticado.
     * @param currentPassword Contraseña actual ingresada por el usuario.
     * @param newPassword     Nueva contraseña que el usuario desea establecer.
     * @return Respuesta indicando éxito o error en el cambio de contraseña.
     */
    @Override
    @Transactional
    public ResponseEntity<?> changePasswordAuthenticated(String username, String currentPassword, String newPassword) {
        try {
            log.info("Buscando usuario con username: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthExceptions.UserNotFoundException("Usuario no encontrado"));
            log.info("Verificando la contraseña actual para el usuario: {}", username);
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.error("La contraseña actual no coincide para el usuario: {}", username);
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "La contraseña actual no es correcta"));
            }
            log.info("Verificando que la nueva contraseña no sea igual a la actual");
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                log.error("La nueva contraseña no puede ser igual a la contraseña actual para el usuario: {}", username);
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "La nueva contraseña no puede ser igual a la contraseña actual"));
            }
            log.info("Actualizando la contraseña para el usuario: {}", username);
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Enviando notificación por correo al usuario: {}", user.getEmail());
            sendPasswordChangeNotification(user.getEmail());

            return ResponseEntity.ok("La contraseña se ha actualizado correctamente");
        } catch (AuthExceptions.UserNotFoundException e) {
            log.error("Error: Usuario no encontrado - {}", e.getMessage());
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Usuario no encontrado"));
        } catch (Exception e) {
            log.error("Error inesperado al cambiar la contraseña: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Error al cambiar la contraseña"));
        }
    }

    /**
     * Envía una notificación al correo del usuario indicando que la contraseña ha sido cambiada.
     *
     * @param email Dirección de correo electrónico del usuario.
     */
    private void sendPasswordChangeNotification(String email) {
        try {
            String subject = "Notificación de cambio de contraseña - TigMotors";
            String content = "<html>" +
                    "<meta charset='UTF-8'>" +
                    "<body style='font-family: Arial, sans-serif;'>" +
                    "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                    "<div style='text-align: center;'>" +
                    "<img src='https://yourcompany.com/logo.png' alt='TigMotors Logo' style='width: 150px; margin-bottom: 20px;' />" +
                    "</div>" +
                    "<h2 style='color: #333;'>Cambio de contraseña exitoso</h2>" +
                    "<p>Estimado usuario,</p>" +
                    "<p>Le informamos que su contraseña ha sido cambiada correctamente. Si usted no realizó esta acción, por favor contacte a nuestro equipo de soporte de inmediato.</p>" +
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

            emailService.sendEmail(email, subject, content);
            log.info("Notificación de cambio de contraseña enviada a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar la notificación de cambio de contraseña: {}", e.getMessage());
            throw new RuntimeException("Error al enviar la notificación de cambio de contraseña", e);
        }
    }


    private String buildResetPasswordEmailContent(String token) {
        return "<html>" +
                "<meta charset='UTF-8'>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<div style='text-align: center;'>" +
                "<img src='https://yourcompany.com/logo.png' alt='TigMotors Logo' style='width: 150px; margin-bottom: 20px;' />" +
                "</div>" +
                "<h2 style='color: #333;'>Recuperación de contraseña</h2>" +
                "<p>Estimado usuario,</p>" +
                "<p>Hemos recibido una solicitud para restablecer su contraseña. Utilice el siguiente código de recuperación:</p>" +
                "<p style='font-size: 18px; font-weight: bold; color: #4CAF50; text-align: center;'>" + token + "</p>" +
                "<p>Para completar el proceso, también puede hacer clic en el botón a continuación para ser redirigido a la página de restablecimiento:</p>" +
                "<div style='text-align: center; margin: 20px 0;'>" +
                "<a href='" + urlCambiarContrasenia + "' style='background-color: #4CAF50; color: white; text-decoration: none; padding: 10px 20px; border-radius: 5px; font-size: 16px;'>Restablecer contraseña</a>" +
                "</div>" +
                "<p>Este enlace es válido durante 15 minutos. Si no solicitó este cambio, ignore este mensaje o comuníquese con nuestro equipo de soporte.</p>" +
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
