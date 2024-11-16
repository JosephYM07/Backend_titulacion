package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminProfileResponse {
    private String username;
    private String businessName;
    private String email;
    private String phoneNumber;
    private String role;
}