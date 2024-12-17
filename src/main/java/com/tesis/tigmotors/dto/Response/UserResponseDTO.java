package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String username;
    private String business_name;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean permiso;
}