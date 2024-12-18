package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.InvalidRequestException;
import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.converters.UserConverter;
import com.tesis.tigmotors.dto.Request.UserRequestDTO;
import com.tesis.tigmotors.dto.Response.ErrorResponse;
import com.tesis.tigmotors.dto.Response.UserResponseDTO;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.utils.RoleValidator;
import org.springframework.security.core.Authentication;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.BusquedaUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusquedaUsuarioServiceImpl implements BusquedaUsuarioService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    private final RoleValidator roleValidator;


    @Override
    public ResponseEntity<?> buscarUsuario(UserRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Validar si el usuario tiene el rol adecuado
        if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
            log.error("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
            throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
        }
        try {

            Optional<User> userOptional;

            // Validar los datos de entrada
            if (request.getId() > 0) { // Validar si el ID es mayor que 0
                userOptional = userRepository.buscarPorId(request.getId());
            } else if (request.getUsername() != null && !request.getUsername().isBlank()) {
                userOptional = userRepository.buscarPorUsername(request.getUsername());
            } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
                userOptional = userRepository.buscarPorEmail(request.getEmail());
            } else {
                throw new InvalidRequestException("Debe proporcionar al menos un campo: ID, username o email.");
            }

            // Verificar si el usuario existe
            User user = userOptional.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

            // Convertir a DTO
            UserResponseDTO responseDTO = userConverter.convertToResponseDTO(user);

            return ResponseEntity.ok(responseDTO);

        } catch (InvalidRequestException ex) {
            log.error("Error de validación al buscar usuario: {}", ex.getMessage(), ex);
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (ResourceNotFoundException ex) {
            log.error("Usuario no encontrado: {}", ex.getMessage(), ex);
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (Exception ex) {
            log.error("Error inesperado al buscar usuario: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al buscar usuario.", ex);
        }
    }
}