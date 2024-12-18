package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Response.StaffProfileResponse;
import com.tesis.tigmotors.dto.Response.UserResponseDTO;
import com.tesis.tigmotors.models.User;
import org.springframework.stereotype.Component;

/**
 * Conversor para manejar la transformación entre entidades User y sus respectivos DTOs de respuesta.
 */
@Component
public class UserConverter {

    /**
     * Convierte una entidad User a un StaffProfileResponse.
     *
     * @param user Entidad User a convertir.
     * @return DTO StaffProfileResponse con los datos del usuario.
     */
    public StaffProfileResponse convertToResponse(User user) {
        return new StaffProfileResponse(
                user.getUsername(),
                user.getBusiness_name(),
                user.getEmail(),
                user.getPhone_number(),
                user.getRole().name()
        );
    }

    /**
     * Convierte una entidad User a un UserResponseDTO.
     *
     * @param user Entidad User a convertir.
     * @return DTO UserResponseDTO con los datos del usuario.
     */
    public UserResponseDTO convertToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getBusiness_name(),
                user.getEmail(),
                user.getPhone_number(),
                user.getRole().toString(),
                user.isPermiso()
        );
    }
}
