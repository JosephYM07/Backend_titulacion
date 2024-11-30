package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserBasicInfoResponseDTO {
    private int id;
    private String username;
    private String businessName;
    private String email;
    private String phoneNumber;
}