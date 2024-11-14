package com.tesis.tigmotors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private String username;
    private String role;
    private String business_name;
    private String email;
    private String phoneNumber;
}
