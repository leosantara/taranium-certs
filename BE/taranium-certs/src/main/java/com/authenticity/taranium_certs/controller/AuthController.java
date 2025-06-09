package com.authenticity.taranium_certs.controller;
// src/main/java/com/taraniumcerts/controller/AuthController.java

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.authenticity.taranium_certs.dto.LoginResponse;
import com.authenticity.taranium_certs.service.GoogleIdTokenVerifierService;
import com.authenticity.taranium_certs.service.UserService;
import com.authenticity.taranium_certs.entity.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * REST Controller untuk mengelola otentikasi dan informasi user.
 */
@RestController
@RequestMapping("/api/auth") // Prefix endpoint for authentication
public class AuthController {

    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final UserService userService;

    public AuthController(GoogleIdTokenVerifierService googleIdTokenVerifierService, UserService userService) {
        this.googleIdTokenVerifierService = googleIdTokenVerifierService;
        this.userService = userService;
    }

    /**
     * DTO untuk menerima ID Token dari frontend.
     */
    @Data
    static class GoogleLoginRequest {
        private String idToken;
    }

    /**
     * Endpoint untuk menerima Google ID Token dari frontend, memverifikasinya,
     * dan mengotentikasi user di backend.
     * @param requestPayload Request body berisi ID Token.
     * @param request HttpServletRequest untuk mengelola sesi.
     * @return LoginResponse yang berisi status login.
     */
    @PostMapping("/google-login")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody GoogleLoginRequest requestPayload, HttpServletRequest request) {
        GoogleIdToken idToken = googleIdTokenVerifierService.verify(requestPayload.getIdToken());

        if (idToken == null) {
            return new ResponseEntity<>(
                    new LoginResponse("Authentication failed", null, null, false),
                    HttpStatus.UNAUTHORIZED
            );
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // Cari atau buat user di database lokal
        AppUser appUser = userService.findOrCreateUser(email, name);

        // Buat objek Authentication untuk Spring Security
        // Ini adalah cara untuk "login" user secara programatik di Spring Security
        UserDetails userDetails = userService.loadUserByUsername(appUser.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // Set objek Authentication ke SecurityContextHolder
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Simpan SecurityContext ke sesi HTTP
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        System.out.println("User " + appUser.getEmail() + " successfully logged in via Google ID Token.");

        return ResponseEntity.ok(
                new LoginResponse(appUser.getName(), appUser.getEmail(), appUser.getRole().name(), true)
        );
    }

    /**
     * Endpoint untuk logout user.
     * Invalidasi sesi Spring Security.
     * @param request HttpServletRequest untuk mengelola sesi.
     * @return Pesan sukses logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Invalidasi sesi HTTP
        }
        SecurityContextHolder.clearContext(); // Hapus konteks keamanan

        System.out.println("User logged out.");
        return ResponseEntity.ok(Collections.singletonMap("message", "Logout berhasil."));
    }

    /**
     * Endpoint untuk mendapatkan informasi user yang sedang login.
     * @param authentication Objek Authentication dari Spring Security.
     * @return LoginResponse yang berisi detail user.
     */
    @GetMapping("/user")
    public ResponseEntity<LoginResponse> getUser(Authentication authentication) {
        LoginResponse response = new LoginResponse();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            response.setAuthenticated(true);
            response.setEmail(userDetails.getUsername());
            response.setRole(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")); // Hapus prefix ROLE_
            // Mendapatkan nama user bisa memerlukan pencarian AppUser dari database jika tidak disimpan di UserDetails
            userService.findByEmail(userDetails.getUsername()).ifPresent(appUser -> response.setName(appUser.getName()));
        } else {
            response.setAuthenticated(false);
            response.setRole("ANONYMOUS");
            response.setName("Guest");
        }
        return ResponseEntity.ok(response);
    }
}
