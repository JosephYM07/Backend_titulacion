package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.AuthResponse;
import com.tesis.tigmotors.dto.LoginRequest;
import com.tesis.tigmotors.dto.RegisterRequest;
import com.tesis.tigmotors.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class UserAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> userLogin(@Valid @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Se deben proporcionar tanto el nombre de usuario como la contraseña.");
        }
        log.info("Solicitud de inicio de sesión recibida para el usuario: " + request.getUsername());
        return authService.login(request);
    }

    @PostMapping("/register")
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

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        log.info("Solicitud recibida para actualizar el token: " + refreshToken);
        return authService.refreshToken(refreshToken);
    }
}
