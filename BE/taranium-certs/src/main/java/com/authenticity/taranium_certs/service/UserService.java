package com.authenticity.taranium_certs.service;
// src/main/java/com/taraniumcerts/service/UserService.java

import com.authenticity.taranium_certs.entity.AppUser;
import com.authenticity.taranium_certs.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service untuk memuat informasi user (AppUser) dari database lokal.
 * Mengimplementasikan UserDetailsService untuk integrasi dengan Spring Security.
 */
@Service
public class UserService implements UserDetailsService { // Ganti dari extends DefaultOAuth2UserService

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Memuat user dari database berdasarkan email (username).
     * Dipanggil oleh Spring Security untuk otentikasi.
     * @param email Email user (digunakan sebagai username).
     * @return UserDetails yang berisi detail user.
     * @throws UsernameNotFoundException jika user tidak ditemukan.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan dengan email: " + email));

        // Buat GrantedAuthority dari role user
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name())
        );

        // Kembalikan objek UserDetails Spring Security
        return new org.springframework.security.core.userdetails.User(
                appUser.getEmail(), // Username
                "", // Password - tidak relevan untuk OAuth2, bisa kosong atau dummy
                authorities
        );
    }

    /**
     * Mencari atau membuat AppUser baru.
     * Ini akan dipanggil setelah ID Token Google diverifikasi.
     * @param email Email user dari Google.
     * @param name Nama user dari Google.
     * @return AppUser yang sudah ada atau yang baru dibuat.
     */
    public AppUser findOrCreateUser(String email, String name) {
        Optional<AppUser> existingUser = appUserRepository.findByEmail(email);
        AppUser appUser;

        if (existingUser.isPresent()) {
            appUser = existingUser.get();
            // Optional: update nama jika berubah
            appUser.setName(name);
            appUserRepository.save(appUser);
            System.out.println("User existing login: " + appUser.getEmail() + " with role " + appUser.getRole());
        } else {
            // Jika user baru, simpan ke database dengan role INSTITUTION
            // Catatan: Dalam aplikasi nyata, proses ini mungkin melibatkan
            // persetujuan admin untuk memberikan role INSTITUTION.
            appUser = new AppUser(email, name, AppUser.UserRole.INSTITUTION);
            appUserRepository.save(appUser);
            System.out.println("New user registered: " + appUser.getEmail() + " with role " + appUser.getRole());
        }
        return appUser;
    }

    /**
     * Mendapatkan AppUser berdasarkan email.
     * @param email Email user.
     * @return Optional AppUser.
     */
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
}
