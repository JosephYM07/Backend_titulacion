package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseUser {
    private Integer id;
    private String username;
    private String role;
    private String business_name;
    private String email;
    private String phone_Number;
    private Boolean permiso;
}
