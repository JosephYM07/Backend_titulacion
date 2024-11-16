package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Response.AdminProfileResponse;
import com.tesis.tigmotors.dto.Request.AdminProfileUpdateRequest;
import com.tesis.tigmotors.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public AdminProfileResponse convertToResponse(User user) {
        return new AdminProfileResponse(
                user.getUsername(),
                user.getBusiness_name(),
                user.getEmail(),
                user.getPhone_number(),
                user.getRole().name()
        );
    }

    public void updateEntityWithRequest(User user, AdminProfileUpdateRequest request) {
        // Validar si el campo `username` está presente en la solicitud
        if (request.getUsername() != null) {
            throw new IllegalArgumentException("No se permite actualizar el nombre de usuario.");
        }

        // Validar y actualizar el nombre del negocio
        if (request.getBusinessName() != null) {
            user.setBusiness_name(request.getBusinessName());
        }

        // Validar y actualizar el correo electrónico
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        // Validar y actualizar el número de teléfono
        if (request.getPhoneNumber() != null) {
            user.setPhone_number(request.getPhoneNumber());
        }
    }
}
