// TokenSendController.java
package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/password-reset-token")
@RequiredArgsConstructor
public class TokenSendController {
    private static final Logger log = LoggerFactory.getLogger(TokenSendController.class);
    private final PasswordResetService passwordResetService;

    @PostMapping("/send-token")
    public String sendResetToken(@RequestParam String email) {
        log.info("Solicitud de envío de código de recuperación para el correo: {}", email);
        try {
            String response = passwordResetService.sendResetToken(email);
            log.info("Código de recuperación enviado exitosamente a: {}", email);
            return response;
        } catch (Exception e) {
            log.error("Error al enviar el código de recuperación al correo: {}. Error: {}", email, e.getMessage());
            throw new RuntimeException("Error al enviar el código de recuperación", e);
        }
    }
}
