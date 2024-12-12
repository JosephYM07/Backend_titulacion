package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.InvalidRequestException;
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
            if (username == null || username.isBlank()) {
                throw new InvalidRequestException("El username es obligatorio y no puede estar vacío.");
            }
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
            throw new RuntimeException("Error inesperado al buscar el perfil del usuario", ex); // Manejado globalmente
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
                throw new InvalidRequestException("El ID del usuario es obligatorio y debe ser mayor que 0.");
            }
            if ((updateRequest.getUsername() == null || updateRequest.getUsername().isBlank()) &&
                    (updateRequest.getBusiness_name() == null || updateRequest.getBusiness_name().isBlank()) &&
                    (updateRequest.getEmail() == null || updateRequest.getEmail().isBlank()) &&
                    (updateRequest.getPhone_Number() == null || updateRequest.getPhone_Number().isBlank())) {
                throw new InvalidRequestException("Debe proporcionar al menos un campo para actualizar.");
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
            if (updateRequest.getPhone_Number() != null && !updateRequest.getPhone_Number().isBlank()) {
                user.setPhone_number(updateRequest.getPhone_Number());
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
            throw ex; // Manejado por el GlobalExceptionHandler
        } catch (InvalidRequestException ex) {
            logger.error("Error de validación en actualización de usuario con ID '{}': {}", updateRequest.getUserId(), ex.getMessage());
            throw ex; // Manejado por el GlobalExceptionHandler
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
            // Verificar que al menos un campo esté presente
            if ((updateRequest.getUsername() == null || updateRequest.getUsername().isBlank()) &&
                    (updateRequest.getBusinessName() == null || updateRequest.getBusinessName().isBlank()) &&
                    (updateRequest.getEmail() == null || updateRequest.getEmail().isBlank()) &&
                    (updateRequest.getPhoneNumber() == null || updateRequest.getPhoneNumber().isBlank())) {
                throw new InvalidRequestException("Debe proporcionar al menos un campo para actualizar.");
            }
            // Buscar al usuario por username
            User user = userRepository.findByUsername(usernameFromToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario con username '" + usernameFromToken + "' no encontrado"));
            // Validar que el usuario tenga el rol USER
            if (!user.getRole().equals(Role.USER)) {
                logger.error("Acceso denegado: el usuario no tiene el rol USER");
                throw new SecurityException("Acceso denegado: solo usuarios con el rol USER pueden acceder a este recurso");
            }
            // Validar que el username proporcionado coincide con el del token
            if (updateRequest.getUsername() != null &&
                    !updateRequest.getUsername().equals(usernameFromToken)) {
                throw new SecurityException("No puede actualizar información de otro usuario.");
            }
            // Actualizar solo los campos presentes y no vacíos
            if (updateRequest.getUsername() != null && !updateRequest.getUsername().isBlank()) {
                user.setUsername(updateRequest.getUsername());
            }
            if (updateRequest.getBusinessName() != null && !updateRequest.getBusinessName().isBlank()) {
                user.setBusiness_name(updateRequest.getBusinessName());
            }
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
                user.setEmail(updateRequest.getEmail());
            }
            if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().isBlank()) {
                user.setPhone_number(updateRequest.getPhoneNumber());
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
        } catch (ResourceNotFoundException | SecurityException | InvalidRequestException ex) {
            logger.error("Error al actualizar el perfil del usuario: {}", usernameFromToken, ex);
            throw ex;
        }
    }

}