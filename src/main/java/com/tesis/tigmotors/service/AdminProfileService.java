package com.tesis.tigmotors.service;

import com.tesis.tigmotors.converters.UserConverter;
import com.tesis.tigmotors.dto.Response.AdminProfileResponse;
import com.tesis.tigmotors.dto.Request.AdminProfileUpdateRequest;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserConverter userConverter;

    public AdminProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return userConverter.convertToResponse(user);
    }

    public AdminProfileResponse updateProfile(String username, AdminProfileUpdateRequest request) {
        // Obtener el usuario actual
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Crear un StringBuilder para registrar los cambios
        StringBuilder updatedFieldsLog = new StringBuilder("Campos actualizados: ");

        // Comparar y registrar cambios
        if (request.getBusinessName() != null && !request.getBusinessName().equals(user.getBusiness_name())) {
            updatedFieldsLog.append(String.format("businessName: '%s' -> '%s', ", user.getBusiness_name(), request.getBusinessName()));
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            updatedFieldsLog.append(String.format("email: '%s' -> '%s', ", user.getEmail(), request.getEmail()));
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhone_number())) {
            updatedFieldsLog.append(String.format("phoneNumber: '%s' -> '%s', ", user.getPhone_number(), request.getPhoneNumber()));
        }

        // Invocar la query personalizada para actualizar en la base de datos
        int rowsAffected = userRepository.updateAdminProfile(
                username,
                request.getBusinessName(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        if (rowsAffected == 0) {
            throw new UsernameNotFoundException("Usuario no encontrado para actualizar");
        }

        // Loggear los cambios realizados
        log.info("Perfil del administrador actualizado correctamente para '{}'. {}", username, updatedFieldsLog.toString());

        // Obtener los datos actualizados para la respuesta
        User updatedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado después de la actualización"));

        return new AdminProfileResponse(
                updatedUser.getUsername(),
                updatedUser.getBusiness_name(),
                updatedUser.getEmail(),
                updatedUser.getPhone_number(),
                updatedUser.getRole().name()
        );
    }

    public void deleteProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        userRepository.delete(user);
    }
}