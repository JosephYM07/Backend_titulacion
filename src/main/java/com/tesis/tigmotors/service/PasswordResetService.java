package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.AuthExceptions;
import com.tesis.tigmotors.models.PasswordResetToken;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.PasswordResetTokenRepository;
import com.tesis.tigmotors.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    // Definir la constante de expiración del token
    private static final long TOKEN_EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public String sendResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthExceptions.UserNotFoundException("Usuario no encontrado con ese correo"));

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
        emailService.sendEmail(email, subject, content);

        log.info("Código de recuperación enviado exitosamente al correo: {}", email);
        return "Código de recuperación enviado al correo";
    }


    @Transactional
    public String resetPassword(String token, String newPassword) {
        log.info("Buscando el token en la base de datos: {}", token);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o caducado"));

        log.info("Token encontrado: {}", resetToken);

        // Verificar si el token ha expirado
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            log.error("El token ha caducado: {}", resetToken);
            throw new RuntimeException("El token ha caducado");
        }

        // Restablecer la contraseña del usuario
        User user = resetToken.getUser();
        log.info("Restableciendo contraseña para el usuario: {}", user.getEmail());

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Eliminar el token después de usarlo
        passwordResetTokenRepository.delete(resetToken);
        log.info("Token eliminado después de restablecer la contraseña: {}", token);

        return "Contraseña actualizada correctamente";
    }

    private String buildResetPasswordEmailContent(String token) {
        return "<html>" +
                "<meta charset='UTF-8'>"+
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<div style='text-align: center;'>" +
                "<img src='https://yourcompany.com/logo.png' alt='TigMotors Logo' style='width: 150px; margin-bottom: 20px;' />" +
                "</div>" +
                "<h2 style='color: #333;'>Recuperación de contraseña</h2>" +
                "<p>Estimado usuario,</p>" +
                "<p>Hemos recibido una solicitud para restablecer su contraseña. Para completar el proceso, utilice el siguiente código de recuperación:</p>" +
                "<p style='font-size: 18px; font-weight: bold; color: #4CAF50; text-align: center;'>" + token + "</p>" +
                "<p>Este código es válido durante 15 minutos. Si no solicitó este cambio, ignore este mensaje o comuníquese con nuestro equipo de soporte.</p>" +
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