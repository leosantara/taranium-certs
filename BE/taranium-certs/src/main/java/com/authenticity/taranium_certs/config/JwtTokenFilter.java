package com.authenticity.taranium_certs.config;

import com.authenticity.taranium_certs.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter kustom Spring Security untuk otentikasi berbasis sesi (cookie JSESSIONID).
 * Filter ini akan memastikan bahwa jika ada sesi Spring Security yang aktif,
 * user akan tetap terotentikasi untuk setiap request.
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtTokenFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Logika utama otentikasi melalui cookie JSESSIONID sudah ditangani oleh Spring Security secara default.
        // Filter ini ditambahkan untuk memastikan bahwa jika ada sesi yang aktif, SecurityContextHolder akan terisi.
        // Ini adalah fallback jika misalnya ada request yang tidak melalui alur filter default Spring Security
        // atau jika Anda ingin menambahkan validasi token kustom (misal, dari Authorization header) di sini.
        // Untuk saat ini, kita mengandalkan sesi Spring Security setelah login Google.

        // Jika user sudah terotentikasi di Spring Security Context, lanjutkan.
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        // Contoh bagaimana Anda akan menangani token dari header (jika ini JWT stateless):
        // String authHeader = request.getHeader("Authorization");
        // if (authHeader != null && authHeader.startsWith("Bearer ")) {
        //     String jwt = authHeader.substring(7);
        //     // Di sini Anda akan memvalidasi JWT dan mendapatkan username/email
        //     // UserDetails userDetails = userService.loadUserByUsername(username);
        //     // Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        //     // SecurityContextHolder.getContext().setAuthentication(authentication);
        // }

        chain.doFilter(request, response);
    }
}
