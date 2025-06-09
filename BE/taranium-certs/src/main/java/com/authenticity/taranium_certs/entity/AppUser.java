package com.authenticity.taranium_certs.entity;


import jakarta.persistence.*;
import lombok.*;

/**
 * Entitas AppUser yang merepresentasikan pengguna institusi.
 * Disimpan di database untuk melacak siapa yang memiliki akses sebagai institusi.
 */
@Entity
@Table(name = "app_users") // Nama tabel di database // Lombok: otomatis membuat getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok: otomatis membuat konstruktor tanpa argumen
@AllArgsConstructor // Lombok: otomatis membuat konstruktor dengan semua argumen
public class AppUser {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Strategi auto-increment ID
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, nullable = false) // Email harus unik dan tidak boleh null
    private String email;

    @Getter
    @Setter
    @Column(nullable = false)
    private String name; // Nama user dari Google

    @Getter
    @Setter
    @Enumerated(EnumType.STRING) // Simpan enum sebagai string di database
    @Column(nullable = false)
    private UserRole role; // Role user: INSTITUTION atau USER_REGULAR

    // Enum untuk peran pengguna
    public enum UserRole {
        INSTITUTION,
        USER_REGULAR // Misalnya, jika ada role user biasa di masa depan
    }

    // Konstruktor tambahan untuk saat registrasi via OAuth2
    public AppUser(String email, String name, UserRole role) {
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
