package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;              // Asegúrate de que es Integer, no int
    private String username;
    private String email;
    private String phoneNumber;      // Respeta el nombre mapeado desde `phone_number`
    private String role;
    private boolean permiso;
}