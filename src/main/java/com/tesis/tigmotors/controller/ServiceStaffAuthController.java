package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.LoginRequest;
import com.tesis.tigmotors.dto.AuthResponse;
import com.tesis.tigmotors.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/service-staff")
@RequiredArgsConstructor
public class ServiceStaffAuthController {

    private final AuthService authService;

    // Endpoint de inicio de sesión para el personal de centro de servicios
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> serviceStaffLogin(@Valid @RequestBody LoginRequest request) {
        return authService.loginAsServiceStaff(request);
    }

}