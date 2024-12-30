package com.tesis.tigmotors.service;

import com.tesis.tigmotors.converters.UserConverter;
import com.tesis.tigmotors.dto.Response.StaffProfileResponse;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.AdminProfileService;
import com.tesis.tigmotors.utils.RoleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminProfileServiceImpl implements AdminProfileService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final RoleValidator roleValidator;


    public StaffProfileResponse getProfile(String username) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // Validar si el usuario tiene el rol adecuado
            if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
                log.error("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
                throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
            }

            log.info("Buscando perfil para el usuario: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario con username '" + username + "' no encontrado"));
            log.info("Perfil encontrado para el usuario: {}", username);
            return userConverter.convertToResponse(user);
        } catch (Exception e) {
            log.error("Error al buscar el perfil del usuario: {}", username, e);
            throw new RuntimeException("Error al buscar el perfil del usuario: " + username, e);
        }
    }

}