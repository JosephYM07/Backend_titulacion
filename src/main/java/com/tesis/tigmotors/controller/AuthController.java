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
import org.springframework.security.core.Authentication;
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

    /**
     * Endpoint público para iniciar sesión en el sistema.
     * Permite autenticar usuarios registrados utilizando sus credenciales.
     *
     * @param request Objeto {@link LoginRequest} que contiene:
     *                - `username`: Nombre de usuario del cliente (obligatorio).
     *                - `password`: Contraseña asociada al usuario (obligatorio).
     * @return Una respuesta {@link AuthResponse} que contiene el token JWT para el usuario autenticado.
     *
     * Validaciones:
     * - Si alguno de los campos `username` o `password` está vacío o es nulo, se genera un error 400 (Bad Request).
     *
     * HTTP:
     * - 200 OK: Inicio de sesión exitoso y se devuelve el token de acceso.
     * - 400 BAD REQUEST: Falta el nombre de usuario o la contraseña.
     * - 401 UNAUTHORIZED: Credenciales incorrectas.
     * - 500 INTERNAL SERVER ERROR: Error interno durante la autenticación.
     */
    @PostMapping("/login-global")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Se deben proporcionar tanto el nombre de usuario como la contraseña.");
        }
        log.info("Solicitud de inicio de sesión recibida para el usuario: " + request.getUsername());
        return authService.login(request);
    }
    /**
     * Endpoint para cerrar sesión de un usuario autenticado.
     *
     * @param authHeader Token JWT incluido en el encabezado Authorization.
     * @param authentication Información del usuario autenticado.
     * @return Respuesta con un mensaje de confirmación.
     */
    @PostMapping("/cerrar-sesion")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {
        // Llamar al servicio de cierre de sesión
        return authService.logout(authHeader, authentication.getName());
    }


    /**
     * Endpoint público para registrar un nuevo usuario en el sistema.
     * Permite a los usuarios proporcionar sus datos para crear una cuenta.
     *
     * @param request Objeto {@link RegisterRequest} que contiene:
     *                - `username`: Nombre de usuario (obligatorio).
     *                - `password`: Contraseña del usuario (obligatorio).
     *                - `email`: Correo electrónico del usuario (obligatorio).
     *                - Otros campos necesarios para el registro.
     * @return Un {@link ResponseEntity} con un mapa que incluye:
     *         - `message`: Mensaje indicando el éxito o el fallo del registro.
     *
     * Validaciones:
     * - Todos los campos requeridos en el objeto `RegisterRequest` deben ser proporcionados y válidos.
     *
     * HTTP:
     * - 200 OK: Registro exitoso y se devuelve un mensaje de confirmación.
     * - 400 BAD REQUEST: Datos inválidos o incompletos en la solicitud.
     * - 500 INTERNAL SERVER ERROR: Error interno durante el proceso de registro.
     */
    @PostMapping("/register-user")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Solicitud de registro recibida para el usuario: {}", request.getUsername());
        return authService.register(request);
    }

    /**
     * Endpoint público para enviar un token de recuperación de contraseña.
     *
     * @param email Dirección de correo del usuario que solicita el código de recuperación.
     * @return Una cadena que indica el resultado del envío.
     *
     * Validaciones:
     * - El correo debe ser válido y registrado en el sistema.
     *
     * Manejo de errores:
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado al enviar el código.
     *
     * Proceso:
     * - Llama al servicio de recuperación de contraseña para generar y enviar el token.
     * - Registra en los logs el resultado de la operación.
     */
    @PostMapping("/send-token")
    public String sendResetToken(@RequestParam String email) {
        log.info("Solicitud de envío de código de recuperación para el correo: {}", email);
        try {
            String response = passwordResetService.sendResetToken(email);
            return response;
        } catch (Exception e) {
            log.error("Error al enviar el código de recuperación al correo: {}. Error: {}", email, e.getMessage());
            throw new RuntimeException("Error al enviar el código de recuperación", e);
        }
    }

    /**
     * Endpoint público para restablecer la contraseña de un usuario.
     *
     * @param resetPasswordRequest Objeto que contiene el token de restablecimiento y la nueva contraseña.
     * @return Respuesta HTTP con un mensaje indicando el éxito o el error del proceso.
     *
     * Validaciones:
     * - El token debe ser válido y corresponder a una solicitud activa.
     * - La nueva contraseña debe cumplir con los requisitos de seguridad establecidos.
     *
     * Manejo de errores:
     * - 400 BAD REQUEST: Si el token no es válido o expira, o si la nueva contraseña no cumple con los requisitos.
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado durante el proceso.
     */
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
