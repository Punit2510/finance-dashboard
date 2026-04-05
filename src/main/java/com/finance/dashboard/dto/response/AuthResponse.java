package com.finance.dashboard.dto.response;

import com.finance.dashboard.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private String username;
    private Role role;
}
