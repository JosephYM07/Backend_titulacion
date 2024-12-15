package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoResponseDTO {
    private int id;
    private String username;
    private String businessName;
    private String email;
    private String phoneNumber;
}