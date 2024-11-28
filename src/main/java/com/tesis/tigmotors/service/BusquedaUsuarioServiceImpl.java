package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.converters.UserConverter;
import com.tesis.tigmotors.dto.Request.UserRequestDTO;
import com.tesis.tigmotors.dto.Response.ErrorResponse;
import com.tesis.tigmotors.dto.Response.UserResponseDTO;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.BusquedaUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class BusquedaUsuarioServiceImpl implements BusquedaUsuarioService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    @Override
    public ResponseEntity<?> buscarUsuario(UserRequestDTO request) {
        try {
            Optional<User> userOptional;

            if (request.getId() != 0) {
                userOptional = userRepository.buscarPorId(request.getId());
            } else if (request.getUsername() != null) {
                userOptional = userRepository.buscarPorUsername(request.getUsername());
            } else if (request.getEmail() != null) {
                userOptional = userRepository.buscarPorEmail(request.getEmail());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Debe proporcionar al menos un campo: ID, username o email."));
            }

            User user = userOptional.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

            UserResponseDTO responseDTO = userConverter.convertToResponseDTO(user);

            return ResponseEntity.ok(responseDTO);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ocurrió un error inesperado."));
        }
    }
}