package com.authenticity.taranium_certs.repository;

import com.authenticity.taranium_certs.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA untuk entitas Certificate.
 * Menyediakan operasi CRUD dasar dan pencarian kustom.
 */
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    /**
     * Mencari Certificate berdasarkan hash dokumen.
     * @param documentHash Hash SHA-256 dokumen.
     * @return Optional yang berisi Certificate jika ditemukan, kosong jika tidak.
     */
    Optional<Certificate> findByDocumentHash(String documentHash);

    /**
     * Mencari semua Certificate yang diupload oleh institusi dengan alamat blockchain tertentu.
     * @param issuerAddress Alamat blockchain institusi.
     * @return List dari Certificate yang diupload oleh institusi tersebut.
     */
    List<Certificate> findByIssuerAddress(String issuerAddress);

    /**
     * Mencari semua Certificate yang diupload oleh institusi tertentu dalam folder tertentu.
     * @param issuerAddress Alamat blockchain institusi.
     * @param folderName Nama folder.
     * @return List dari Certificate yang cocok.
     */
    List<Certificate> findByIssuerAddressAndFolderName(String issuerAddress, String folderName);
}
