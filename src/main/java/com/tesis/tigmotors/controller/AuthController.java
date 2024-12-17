package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.LoginRequest;
import com.tesis.tigmotors.dto.Request.RegisterRequest;
import com.tesis.tigmotors.dto.Request.ResetPasswordRequest;
import com.tesis.tigmotors.dto.Response.AuthResponse;
import com.tesis.tigmotors.service.interfaces.AuthService;
import com.tesis.tigmotors.service.interfaces.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para manejar la autenticación y el restablecimiento de contraseñas.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login-global")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Se deben proporcionar tanto el nombre de usuario como la contraseña.");
        }
        log.info("Solicitud de inicio de sesión recibida para el usuario: " + request.getUsername());
        return authService.login(request);
    }

    @PostMapping("/register-user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().isEmpty() ||
                    request.getPassword() == null || request.getPassword().isEmpty() ||
                    request.getEmail() == null || request.getEmail().isEmpty()) {
                throw new IllegalArgumentException("Se deben proporcionar tanto el nombre de usuario, la contraseña y el correo electrónico.");
            }
        } catch (IllegalArgumentException e) {
            log.error("Error al intentar registrar al usuario: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        log.info("Solicitud de registro recibida para el usuario: " + request.getUsername());
        return authService.register(request);
    }

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

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        log.info("Solicitud de restablecimiento de contraseña recibida");
        log.debug("Datos recibidos - Token: {}, Nueva Contraseña: [PROTEGIDO]", resetPasswordRequest.getToken());

        Map<String, String> response = new HashMap<>();
        try {
            String resultMessage = passwordResetService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
            response.put("message", resultMessage);
            log.info("Contraseña restablecida exitosamente para el token: {}", resetPasswordRequest.getToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error al restablecer la contraseña: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}
