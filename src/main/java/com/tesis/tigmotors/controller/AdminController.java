package com.tesis.tigmotors.controller;


import com.tesis.tigmotors.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tesis.tigmotors.dto.Request.AdminProfileUpdateRequest;
import com.tesis.tigmotors.dto.Response.AdminProfileResponse;
import com.tesis.tigmotors.service.AdminProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminProfileService adminProfileService;

    @GetMapping("/users/status")
    public ResponseEntity<Object> getUsersStatus() {
        return userService.getUsersStatus();
    }

    @GetMapping("/users/pending")
    public ResponseEntity<Object> getPendingUsers() {
        return userService.getPendingUsers();
    }

    @PutMapping("/users/approve/{userId}")
    public ResponseEntity<Object> approveUser(@PathVariable Integer userId) {
        return userService.approveUser(userId);
    }

    //Crud

    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para el administrador: {}", username);
        return ResponseEntity.ok(adminProfileService.getProfile(username));
    }

    @PostMapping("/update")
    public ResponseEntity<AdminProfileResponse> updateProfile(Authentication authentication, @RequestBody @Valid AdminProfileUpdateRequest request) {
        String username = authentication.getName();
        log.info("Actualizando perfil del administrador: {}", username);
        return ResponseEntity.ok(adminProfileService.updateProfile(username, request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Eliminando perfil del administrador: {}", username);
        adminProfileService.deleteProfile(username);
        return ResponseEntity.ok(Map.of("message", "Perfil eliminado con éxito"));
    }
}