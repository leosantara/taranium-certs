package com.authenticity.taranium_certs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk respons status login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String name;
    private String email; // Tambahkan field email
    private String role;
    private boolean isAuthenticated; // Tambahkan field isAuthenticated
}
