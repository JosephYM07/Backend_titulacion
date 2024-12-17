package com.tesis.tigmotors.utils;

import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.tesis.tigmotors.enums.Role;

@Component
@NoArgsConstructor
public class RoleValidator {

    public boolean tieneRol(Authentication authentication, Role rol) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(rol.name()));
    }

    public boolean tieneAlgunRol(Authentication authentication, Role... roles) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> {
                    for (Role rol : roles) {
                        if (authority.getAuthority().equals(rol.name())) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}