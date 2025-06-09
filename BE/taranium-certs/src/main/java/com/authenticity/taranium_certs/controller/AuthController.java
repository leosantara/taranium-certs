package com.authenticity.taranium_certs.controller;
// src/main/java/com/taraniumcerts/controller/AuthController.java

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.authenticity.taranium_certs.dto.LoginResponse;
import com.authenticity.taranium_certs.service.GoogleIdTokenVerifierService;
import com.authenticity.taranium_certs.service.UserService;
import com.authenticity.taranium_certs.entity.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpHeaders; // Import baru
import org.springframework.http.MediaType; // Import baru
import org.springframework.util.LinkedMultiValueMap; // Import baru
import org.springframework.util.MultiValueMap; // Import baru

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

    @Value("${app.google.client-id}") // Client ID dari application.yml
    private String googleClientId;

    @Value("${app.google.client-secret}") // Client Secret dari application.yml
    private String googleClientSecret;

    // URL redirect yang sama persis seperti yang terdaftar di Google Cloud Console untuk backend
    // Biasanya http://localhost:8080/oauth2/callback atau sejenisnya, ini penting!
    @Value("${app.google.redirect-uri}")
    private String googleBackendRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate(); // Untuk membuat permintaan HTTP

    public AuthController(GoogleIdTokenVerifierService googleIdTokenVerifierService, UserService userService) {
        this.googleIdTokenVerifierService = googleIdTokenVerifierService;
        this.userService = userService;
    }

    /**
     * DTO untuk menerima Authorization Code dari frontend.
     */
    @Data
    static class GoogleAuthCodeRequest {
        private String code;
    }

    /**
     * Endpoint untuk menerima Authorization Code dari frontend, menukarnya dengan ID Token di Google,
     * memverifikasinya, dan mengotentikasi user di backend.
     * @param requestPayload Request body berisi Authorization Code.
     * @param request HttpServletRequest untuk mengelola sesi.
     * @return LoginResponse yang berisi status login.
     */
    @PostMapping("/google-auth-code") // <--- PERUBAHAN: Endpoint baru untuk menerima kode otorisasi
    public ResponseEntity<LoginResponse> googleAuthCodeLogin(@RequestBody GoogleAuthCodeRequest requestPayload, HttpServletRequest request) {
        String code = requestPayload.getCode();
        if (code == null || code.isEmpty()) {
            System.err.println("Auth code is missing in request payload.");
            return new ResponseEntity<>(
                    new LoginResponse("Authorization code is missing.", null, null, false),
                    HttpStatus.BAD_REQUEST
            );
        }

        System.out.println("Received auth code from frontend: " + code.substring(0, Math.min(code.length(), 20)) + "..."); // Log sebagian kode

        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        // Siapkan header dan body untuk permintaan form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // <--- KUNCI PERBAIKAN

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", googleClientId);
        map.add("client_secret", googleClientSecret);
        map.add("redirect_uri", googleBackendRedirectUri);
        map.add("grant_type", "authorization_code");

        org.springframework.http.HttpEntity<MultiValueMap<String, String>> httpEntity = new org.springframework.http.HttpEntity<>(map, headers); // <--- KUNCI PERBAIKAN

        try {
            System.out.println("Attempting to exchange auth code with Google Token Endpoint...");
            System.out.println("  Client ID: " + googleClientId);
            System.out.println("  Redirect URI: " + googleBackendRedirectUri);
            // WARNING: Jangan log client secret!

            // Lakukan permintaan POST ke Google Token Endpoint dengan HttpEntity
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenEndpoint, httpEntity, String.class); // <--- KUNCI PERBAIKAN

            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
                System.err.println("Failed to exchange auth code for tokens. Status: " + tokenResponse.getStatusCode());
                System.err.println("Response Body: " + tokenResponse.getBody());
                return new ResponseEntity<>(
                        new LoginResponse("Failed to exchange code for tokens.", null, null, false),
                        HttpStatus.UNAUTHORIZED
                );
            }

            JsonObject jsonResponse = new Gson().fromJson(tokenResponse.getBody(), JsonObject.class);
            String idTokenString = jsonResponse.get("id_token").getAsString();
            System.out.println("Successfully exchanged code for tokens. ID Token received.");

            GoogleIdToken idToken = googleIdTokenVerifierService.verify(idTokenString);

            if (idToken == null) {
                System.err.println("Authentication failed: ID Token verification failed.");
                return new ResponseEntity<>(
                        new LoginResponse("Authentication failed: Invalid ID Token.", null, null, false),
                        HttpStatus.UNAUTHORIZED
                );
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            AppUser appUser = userService.findOrCreateUser(email, name);

            UserDetails userDetails = userService.loadUserByUsername(appUser.getEmail());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            System.out.println("User " + appUser.getEmail() + " successfully logged in via Google Auth Code Flow and session created.");

            return ResponseEntity.ok(
                    new LoginResponse(appUser.getName(), appUser.getEmail(), appUser.getRole().name(), true)
            );

        } catch (HttpClientErrorException e) {
            // Tangani error HTTP client (misal 400 Bad Request dari Google API)
            System.err.println("HTTP Client Error during Google auth code flow: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return new ResponseEntity<>(
                    new LoginResponse("Authentication failed: " + e.getResponseBodyAsString(), null, null, false),
                    HttpStatus.UNAUTHORIZED // Bisa juga HttpStatus.BAD_REQUEST tergantung pesan error Google
            );
        } catch (Exception e) {
            System.err.println("Error during Google auth code flow: " + e.getMessage());
            e.printStackTrace(); // Cetak stack trace untuk debug lebih lanjut
            return new ResponseEntity<>(
                    new LoginResponse("Internal server error during authentication.", null, null, false),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
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
            response.setRole(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
            userService.findByEmail(userDetails.getUsername()).ifPresent(appUser -> response.setName(appUser.getName()));
        } else {
            response.setAuthenticated(false);
            response.setRole("ANONYMOUS");
            response.setName("Guest");
        }
        return ResponseEntity.ok(response);
    }
}