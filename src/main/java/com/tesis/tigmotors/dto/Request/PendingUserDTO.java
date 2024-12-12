package com.tesis.tigmotors.dto.Request;

import com.tesis.tigmotors.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PendingUserDTO {
    private Integer id;
    private String username;
    private String businessName;
    private String phoneNumber;
    private Role role;
    private String email;
    private Boolean permiso;
}
