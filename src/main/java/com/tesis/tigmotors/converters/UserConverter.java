package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Response.StaffProfileResponse;
import com.tesis.tigmotors.dto.Response.UserResponseDTO;
import com.tesis.tigmotors.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public StaffProfileResponse convertToResponse(User user) {
        return new StaffProfileResponse(
                user.getUsername(),
                user.getBusiness_name(),
                user.getEmail(),
                user.getPhone_number(),
                user.getRole().name()
        );
    }

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
