package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.LoginRequest;
import com.tesis.tigmotors.dto.Request.RegisterRequest;
import com.tesis.tigmotors.dto.Response.AuthResponse;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthService {

    /**
     * Método para iniciar sesión de un usuario.
     * @param request Objeto que contiene las credenciales del usuario.
     * @return ResponseEntity con el token de acceso o un mensaje de error.
     */
    ResponseEntity<AuthResponse> login(LoginRequest request);

    /**
     * Método para registrar un nuevo usuario.
     * @param request Objeto que contiene la información de registro del usuario.
     * @return ResponseEntity con un mensaje de éxito o de error.
     */
    ResponseEntity<Map<String, String>> register(RegisterRequest request);

    /**
     * Método para restablecer la contraseña de un usuario.
     * @param email Correo del usuario.
     * @return ResponseEntity con un mensaje de éxito o de error.
     */
    // Firma del método para registrar un usuario por parte del administrador
    ResponseEntity<Map<String, String>> registerByAdmin(RegisterRequest request, String adminUsername);
}
