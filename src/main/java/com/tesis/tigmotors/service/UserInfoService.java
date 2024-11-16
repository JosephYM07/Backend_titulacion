package com.tesis.tigmotors.service;

import com.tesis.tigmotors.dto.Response.UserInfoResponse;
import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.models.User;
import com.tesis.tigmotors.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserRepository userRepository;

    public UserInfoResponse getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return new UserInfoResponse(
                user.getUsername(),
                user.getRole().name(),
                user.getBusiness_name(),
                user.getEmail(),
                user.getPhone_number()
        );
    }
}
