package com.tesis.tigmotors.service;

import com.tesis.tigmotors.converters.UserConverter;
import com.tesis.tigmotors.dto.Response.StaffProfileResponse;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.AdminProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminProfileServiceImpl implements AdminProfileService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    public StaffProfileResponse getProfile(String username) {
        try {
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