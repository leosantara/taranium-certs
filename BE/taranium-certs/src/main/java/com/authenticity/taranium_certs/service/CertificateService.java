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
     * Mengambil semua sertifikat yang diupload oleh institusi dengan alamat blockchain tertentu.
     * @param issuerAddress Alamat blockchain institusi.
     * @return List dari Certificate.
     */
    public List<Certificate> getCertificatesByIssuerAddress(String issuerAddress) {
        return certificateRepository.findByIssuerAddress(issuerAddress);
    }

    /**
     * Mengambil semua sertifikat yang diupload oleh institusi tertentu dalam folder tertentu.
     * @param issuerAddress Alamat blockchain institusi.
     * @param folderName Nama folder.
     * @return List dari Certificate.
     */
    public List<Certificate> getCertificatesByIssuerAddressAndFolder(String issuerAddress, String folderName) {
        return certificateRepository.findByIssuerAddressAndFolderName(issuerAddress, folderName);
    }
}