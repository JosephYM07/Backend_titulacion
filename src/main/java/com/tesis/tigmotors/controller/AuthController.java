package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.LoginRequest;
import com.tesis.tigmotors.dto.Request.RegisterRequest;
import com.tesis.tigmotors.dto.Response.AuthResponse;
import com.tesis.tigmotors.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    /*@PostMapping("/login-users")
    public ResponseEntity<AuthResponse> userLogin(@Valid @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Se deben proporcionar tanto el nombre de usuario como la contraseña.");
        }
        log.info("Solicitud de inicio de sesión recibida para el usuario: " + request.getUsername());
        return authService.loginAsUser(request);
    }

    // Endpoint de inicio de sesión para administradores
    @PostMapping("/admin-login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        return authService.loginAsAdmin(request);
    }

    // Endpoint de inicio de sesión para el personal de centro de servicios
    @PostMapping("/service-staff-login")
    public ResponseEntity<AuthResponse> serviceStaffLogin(@Valid @RequestBody LoginRequest request) {
        return authService.loginAsServiceStaff(request);
    }*/
    @PostMapping("/login-global")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Se deben proporcionar tanto el nombre de usuario como la contraseña.");
        }
        log.info("Solicitud de inicio de sesión recibida para el usuario: " + request.getUsername());
        return authService.login(request);
    }


    //Endpoint para registrar solo usuarios
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

/*    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        log.info("Solicitud recibida para actualizar el token: " + refreshToken);
        return authService.refreshToken(refreshToken);
    }
    */
}
