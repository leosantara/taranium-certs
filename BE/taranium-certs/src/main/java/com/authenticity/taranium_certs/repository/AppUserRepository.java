package com.authenticity.taranium_certs.repository;

import com.authenticity.taranium_certs.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository JPA untuk entitas AppUser.
 * Menyediakan operasi CRUD dasar dan pencarian berdasarkan email.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Mencari AppUser berdasarkan email.
     * @param email Email user.
     * @return Optional yang berisi AppUser jika ditemukan, kosong jika tidak.
     */
    Optional<AppUser> findByEmail(String email);
}
