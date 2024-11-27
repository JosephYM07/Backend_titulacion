package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.dto.Request.UserUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.UserResponseUser;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceUpdate {

    private final UserRepository userRepository;

    public UserResponseUser updateUser(UserUpdateRequestDTO updateRequest) {
        // Validar que el usuario existe
        User user = userRepository.findById(updateRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Actualizar solo los campos presentes y no vacíos
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().isBlank()) {
            user.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getBusiness_name() != null && !updateRequest.getBusiness_name().isBlank()) {
            user.setBusiness_name(updateRequest.getBusiness_name());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone_number() != null && !updateRequest.getPhone_number().isBlank()) {
            user.setPhone_number(updateRequest.getPhone_number());
        }

        // Guardar los cambios
        userRepository.save(user);

        // Retornar los datos actualizados
        return new UserResponseUser(
                user.getId(),
                user.getUsername(),
                user.getBusiness_name(),
                user.getEmail(),
                user.getPhone_number(),
                user.getRole().name(),
                user.isPermiso()
        );
    }
}
