package com.tesis.tigmotors.service;

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
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public String sendResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese correo"));

        // Log antes de la eliminación
        log.info("Eliminando cualquier token anterior del usuario: {}", user.getEmail());

        // Eliminar tokens previos del usuario, si existen
        passwordResetTokenRepository.deleteByUser(user);

        // Generar un nuevo token y fecha de expiración
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(15, ChronoUnit.MINUTES);

        log.info("Generando nuevo token para el usuario: {}", user.getEmail());

        // Guardar el token en la base de datos
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .expiryDate(expiryDate)
                .user(user)
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        // Enviar el correo con el token
        emailService.sendEmail(email, "Código de recuperación de contraseña", "Su código de recuperación es: " + token);
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
}