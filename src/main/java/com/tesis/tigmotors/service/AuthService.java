package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.AuthExceptions;
import com.tesis.tigmotors.dto.AuthResponse;
import com.tesis.tigmotors.dto.LoginRequest;
import com.tesis.tigmotors.dto.RegisterRequest;
import com.tesis.tigmotors.models.RefreshToken;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.roles.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public ResponseEntity<AuthResponse> loginAsUser(LoginRequest request) {
        return loginWithRole(request, Role.USER);
    }

    public ResponseEntity<AuthResponse> loginAsAdmin(LoginRequest request) {
        return loginWithRole(request, Role.ADMIN);
    }

    public ResponseEntity<AuthResponse> loginAsServiceStaff(LoginRequest request) {
        return loginWithRole(request, Role.PERSONAL_CENTRO_DE_SERVICIOS);
    }

    private ResponseEntity<AuthResponse> loginWithRole(LoginRequest request, Role expectedRole) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AuthExceptions.UserNotFoundException("Usuario no encontrado"));

            if (!user.getRole().equals(expectedRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthResponse.builder()
                        .status("Error")
                        .message("Acceso denegado para este rol")
                        .build());
            }
            if (!user.isPermiso()) {
                log.warn("Usuario no aprobado: " + user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder()
                        .status("Error")
                        .message("Su cuenta aún no ha sido aprobada por el administrador")
                        .build());
            }
            String accessToken = jwtService.generateAccessToken(user);
            return ResponseEntity.ok(AuthResponse.builder()
                    .status("success")
                    .message("Autenticación exitosa")
                    .token(accessToken)
                    .build());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder()
                    .status("Error")
                    .message("Nombre de usuario o contraseña no válidos")
                    .build());
        }
    }

    public ResponseEntity<AuthResponse> register(RegisterRequest request) {
        try {
            // Verificar si el nombre de usuario ya está en uso en la base de datos y correo electrónico
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponse.builder()
                        .status("Error")
                        .message("El nombre de usuario ya está en uso")
                        .build());
            }
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponse.builder()
                        .status("Error")
                        .message("El correo electrónico ya está en uso")
                        .build());
            }
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .business_name(request.getBusiness_name())
                    .email(request.getEmail())
                    .phone_number(request.getPhone_number())
                    .role(Role.USER)
                    .permiso(false)
                    .build();

            userRepository.save(user);
            String accessToken = jwtService.generateAccessToken(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                    .message("Registro exitoso, espere a que el administrador apruebe su cuenta para poder iniciar sesión")
                    .build());
        } catch (Exception e) {
            log.error("Error al registrar usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthResponse.builder()
                    .status("Error")
                    .message("Error al registrar usuario")
                    .build());
        }
    }

    public ResponseEntity<AuthResponse> refreshToken(String refreshToken) {
        Optional<RefreshToken> storedToken = refreshTokenService.findByToken(refreshToken);

        if (storedToken.isEmpty() || storedToken.get().getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.builder()
                            .status("error")
                            .message("Actualizar token caducado o inválido")
                            .build()
            );
        }

        User user = storedToken.get().getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .status("success")
                .message("Nuevo token de acceso generado")
                .token(newAccessToken)
                .build());
    }
}
