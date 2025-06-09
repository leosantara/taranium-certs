package com.authenticity.taranium_certs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer; // Import ini
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity; // Import ini
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

/**
 * Konfigurasi keamanan untuk aplikasi Spring Boot.
 * Mengatur otentikasi berbasis sesi setelah ID Token diverifikasi di endpoint kustom.
 */
@Configuration
@EnableWebSecurity
// Jika Anda memang membutuhkan anotasi keamanan method seperti @PreAuthorize, biarkan ini.
// Jika tidak, Anda bisa menghapusnya untuk kesederhanaan.
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    /**
     * Mendefinisikan konfigurasi CORS secara eksplisit sebagai Bean.
     * Ini akan diambil oleh .cors(Customizer.withDefaults()).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Penting untuk mengirim cookie sesi
        // Izinkan origin frontend Anda
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Metode yang diizinkan
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")); // Header yang diizinkan
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")); // Header yang diekspos
        source.registerCorsConfiguration("/**", config); // Terapkan konfigurasi ke semua path
        return source;
    }

    /**
     * Mendefinisikan rantai filter keamanan HTTP.
     * @param http Objek HttpSecurity untuk mengkonfigurasi keamanan web.
     * @return SecurityFilterChain yang sudah dikonfigurasi.
     * @throws Exception jika terjadi kesalahan konfigurasi.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Terapkan CORS menggunakan Customizer.withDefaults()
                .cors(Customizer.withDefaults())
                // Nonaktifkan CSRF
                .csrf(csrf -> csrf.disable())
                // Konfigurasi otorisasi permintaan HTTP
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // Izinkan akses publik ke endpoint tertentu
                                .requestMatchers(
                                        "/api/user",          // Info user/status login
                                        "/api/verify",        // Verifikasi sertifikat
                                        "/api/auth/google-login", // Login Google (menerima ID Token)
                                        "/api/auth/logout",   // Logout
                                        "/"                   // Halaman utama
                                ).permitAll()

                                // Hanya user dengan role INSTITUTION yang bisa mengakses endpoint registrasi dan folder
                                .requestMatchers("/api/register", "/api/folders/**").hasRole("INSTITUTION")
                                // Semua request lainnya memerlukan otentikasi
                                .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        // Tangani Unauthorized (401)
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                // Konfigurasi manajemen sesi: IF_REQUIRED karena kita menggunakan sesi JSESSIONID
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // Tambahkan filter kustom Anda sebelum filter autentikasi standar
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
