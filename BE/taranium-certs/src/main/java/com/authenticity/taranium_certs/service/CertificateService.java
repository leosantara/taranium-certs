package com.authenticity.taranium_certs.service;


import com.authenticity.taranium_certs.entity.Certificate;
import com.authenticity.taranium_certs.repository.CertificateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service untuk mengelola entitas Certificate di database lokal.
 * Bertanggung jawab atas operasi CRUD untuk metadata sertifikat.
 */
@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    /**
     * Menyimpan metadata sertifikat baru ke database.
     * @param certificate Objek Certificate yang akan disimpan.
     * @return Certificate yang telah disimpan.
     */
    public Certificate saveCertificateMetadata(Certificate certificate) {
        return certificateRepository.save(certificate);
    }

    /**
     * Mencari metadata sertifikat berdasarkan hash dokumen.
     * @param documentHash Hash SHA-256 dokumen.
     * @return Optional yang berisi Certificate jika ditemukan.
     */
    public Optional<Certificate> getCertificateByHash(String documentHash) {
        return certificateRepository.findByDocumentHash(documentHash);
    }

    /**
     * Mengambil semua sertifikat yang diupload oleh institusi tertentu.
     * @param issuerEmail Email institusi.
     * @return List dari Certificate.
     */
    public List<Certificate> getCertificatesByIssuerEmail(String issuerEmail) {
        return certificateRepository.findByIssuerEmail(issuerEmail);
    }

    /**
     * Mengambil semua sertifikat yang diupload oleh institusi tertentu dalam folder tertentu.
     * @param issuerEmail Email institusi.
     * @param folderName Nama folder.
     * @return List dari Certificate.
     */
    public List<Certificate> getCertificatesByIssuerEmailAndFolder(String issuerEmail, String folderName) {
        return certificateRepository.findByIssuerEmailAndFolderName(issuerEmail, folderName);
    }
}
