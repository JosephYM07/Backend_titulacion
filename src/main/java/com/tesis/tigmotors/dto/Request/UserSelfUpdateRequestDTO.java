package com.tesis.tigmotors.dto.Request;

import lombok.Data;

@Data
public class UserSelfUpdateRequestDTO {
    private String business_name;
    private String email;
    private String phone_number;
}
