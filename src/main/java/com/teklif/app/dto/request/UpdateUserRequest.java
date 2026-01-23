package com.teklif.app.dto.request;

import com.teklif.app.enums.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private Role role;
    private Boolean isActive;
}