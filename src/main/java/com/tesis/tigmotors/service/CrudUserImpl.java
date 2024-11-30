package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.dto.Request.UserSelfUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.UserBasicInfoResponseDTO;
import com.tesis.tigmotors.dto.Request.UserUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.UserResponseUser;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.UserServiceUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class CrudUserImpl implements UserServiceUpdate {

    private static final Logger logger = LoggerFactory.getLogger(CrudUserImpl.class);

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserBasicInfoResponseDTO getUserProfile(String username) {
        try {
            logger.info("Buscando perfil para el usuario: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario con username '" + username + "' no encontrado"));
            logger.info("Rol del usuario encontrado: {}", user.getRole());
            if (!user.getRole().equals(Role.USER)) {
                logger.error("Acceso denegado: el usuario no tiene el rol USER");
                throw new SecurityException("Acceso denegado: solo usuarios con el rol USER pueden acceder a este recurso");
            }
            logger.info("Perfil encontrado para el usuario: {}", username);
            return new UserBasicInfoResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getBusiness_name(),
                    user.getEmail(),
                    user.getPhone_number()
            );
        } catch (ResourceNotFoundException ex) {
            logger.error("Usuario no encontrado: {}", username, ex);
            throw ex;
        } catch (SecurityException ex) {
            logger.error("Acceso denegado para el usuario: {}", username, ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Error inesperado al buscar el perfil del usuario: {}", username, ex);
            throw new RuntimeException("Error inesperado al buscar el perfil del usuario", ex);
        }
    }

    /**
     * Actualiza los datos de un usuario desde administrador.
     * @param updateRequest Objeto con los datos a actualizar.
     * @return UserResponseUser con los datos actualizados.
     */
    @Override
    @Transactional
    public UserResponseUser updateUser(UserUpdateRequestDTO updateRequest, String adminUsername) {
        logger.info("Administrador '{}' intentando actualizar información del usuario con ID: {}", adminUsername, updateRequest.getUserId());

        try {
            // Validar que el ID del usuario esté presente
            if (updateRequest.getUserId() == null || updateRequest.getUserId() <= 0) {
                throw new IllegalArgumentException("El ID del usuario es obligatorio y debe ser mayor que 0.");
            }
            User user = userRepository.findById(updateRequest.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID '" + updateRequest.getUserId() + "' no encontrado"));

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
            userRepository.save(user);
            logger.info("Información del usuario con ID '{}' actualizada correctamente por el administrador '{}'", user.getId(), adminUsername);
            return new UserResponseUser(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name(),
                    user.getBusiness_name(),
                    user.getEmail(),
                    user.getPhone_number(),
                    user.isPermiso()
            );
        } catch (ResourceNotFoundException ex) {
            logger.error("Usuario no encontrado con ID: {}", updateRequest.getUserId(), ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.error("Error de validación: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Error inesperado al actualizar el usuario con ID '{}': {}", updateRequest.getUserId(), ex.getMessage());
            throw new RuntimeException("Error inesperado al actualizar el perfil del usuario", ex);
        }
    }


    @Override
    @Transactional
    public UserBasicInfoResponseDTO updateMyProfile(UserSelfUpdateRequestDTO updateRequest, String usernameFromToken) {
        logger.info("Actualizando información del usuario autenticado: {}", usernameFromToken);

        try {
            // Buscar al usuario por username
            User user = userRepository.findByUsername(usernameFromToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario con username '" + usernameFromToken + "' no encontrado"));

            // Validar que el usuario tenga el rol USER
            if (!user.getRole().equals(Role.USER)) {
                logger.error("Acceso denegado: el usuario no tiene el rol USER");
                throw new SecurityException("Acceso denegado: solo usuarios con el rol USER pueden acceder a este recurso");
            }

            // Actualizar solo los campos presentes y no vacíos
            if (updateRequest.getBusiness_name() != null && !updateRequest.getBusiness_name().isBlank()) {
                user.setBusiness_name(updateRequest.getBusiness_name());
            }
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
                user.setEmail(updateRequest.getEmail());
            }
            if (updateRequest.getPhone_number() != null && !updateRequest.getPhone_number().isBlank()) {
                user.setPhone_number(updateRequest.getPhone_number());
            }

            // Guardar los cambios en la base de datos
            userRepository.save(user);

            logger.info("Perfil actualizado correctamente para el usuario: {}", usernameFromToken);

            // Retornar solo los datos necesarios usando UserBasicInfoResponseDTO
            return new UserBasicInfoResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getBusiness_name(),
                    user.getEmail(),
                    user.getPhone_number()
            );
        } catch (Exception ex) {
            logger.error("Error al actualizar la información del usuario: {}", usernameFromToken, ex);
            throw new RuntimeException("Error inesperado al actualizar el perfil del usuario", ex);
        }
    }
}