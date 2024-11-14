package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.UserInfoResponse;
import com.tesis.tigmotors.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminInfoController {

    private final UserInfoService userInfoService;

    // Endpoint para obtener la información del usuario autenticado
    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        UserInfoResponse userInfo = userInfoService.getUserInfo(authentication);
        return ResponseEntity.ok(userInfo);
    }
}
