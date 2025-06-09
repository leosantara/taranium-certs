package com.authenticity.taranium_certs.controller;

// src/main/java/com/taraniumcerts/controller/CertificateController.java

import com.authenticity.taranium_certs.dto.CertificateResponse;
import com.authenticity.taranium_certs.entity.Certificate;
import com.authenticity.taranium_certs.exception.StorageException;
import com.authenticity.taranium_certs.service.*;
import com.authenticity.taranium_certs.service.HashService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller untuk mengelola operasi sertifikat: pendaftaran dan verifikasi.
 * Autentikasi dan otorisasi dilakukan sepenuhnya di sisi frontend (dengan MetaMask).
 * Backend ini hanya fokus pada hashing, penyimpanan file, dan menyediakan metadata.
 */
@RestController
@RequestMapping("/api")
public class CertificateController {

    private final HashService hashService;
    private final FileStorageService fileStorageService;
    private final CertificateService certificateService;

    public CertificateController(HashService hashService, FileStorageService fileStorageService, CertificateService certificateService) {
        this.hashService = hashService;
        this.fileStorageService = fileStorageService;
        this.certificateService = certificateService;
    }

    /**
     * Endpoint untuk mendaftarkan hash sertifikat dan menyimpannya secara lokal.
     * Frontend akan mengirimkan issuerAddress (alamat MetaMask institusi) sebagai parameter.
     * @param file File sertifikat yang diunggah.
     * @param folderName Nama folder tempat file akan disimpan lokal.
     * @param issuerAddress Alamat MetaMask institusi yang mengunggah.
     * @return ResponseEntity yang berisi hash, path file lokal, dan pesan.
     */
    @PostMapping("/register")
    public ResponseEntity<CertificateResponse> registerCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderName") String folderName,
            @RequestParam("issuerAddress") String issuerAddress) {

        if (file.isEmpty()) {
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("File tidak boleh kosong!").build(),
                    HttpStatus.BAD_REQUEST
            );
        }
        if (folderName == null || folderName.trim().isEmpty()) {
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("Nama folder tidak boleh kosong!").build(),
                    HttpStatus.BAD_REQUEST
            );
        }
        if (issuerAddress == null || issuerAddress.trim().isEmpty()) {
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("Alamat institusi (MetaMask) tidak boleh kosong!").build(),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            // 1. Hitung hash SHA-256 dari file yang diunggah
            String documentHash = hashService.calculateSha256Hash(file.getInputStream());

            // 2. Simpan file secara lokal
            String localFilePath = fileStorageService.storeFile(file, issuerAddress, folderName);

            // 3. Simpan metadata sertifikat ke database lokal
            Certificate certificate = new Certificate(documentHash, file.getOriginalFilename(), localFilePath, folderName, issuerAddress);
            certificateService.saveCertificateMetadata(certificate);

            // Backend hanya mengembalikan hash dan path. Frontend akan melanjutkan interaksi ke blockchain.
            return new ResponseEntity<>(
                    CertificateResponse.builder()
                            .documentHash(documentHash)
                            .originalFileName(file.getOriginalFilename())
                            .localFilePath(localFilePath)
                            .folderName(folderName)
                            .issuerAddress(issuerAddress)
                            .uploadTimestamp(certificate.getUploadTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .message("File berhasil diunggah, di-hash, dan metadata disimpan.")
                            .build(),
                    HttpStatus.OK
            );

        } catch (StorageException e) {
            System.err.println("Error penyimpanan file: " + e.getMessage());
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("Error penyimpanan file: " + e.getMessage()).build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algoritma hash tidak ditemukan: " + e.getMessage());
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("Error server: Algoritma hash tidak tersedia.").build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            System.err.println("Error umum saat mendaftarkan sertifikat: " + e.getMessage());
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("Terjadi kesalahan yang tidak terduga: " + e.getMessage()).build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Endpoint untuk memverifikasi hash sertifikat.
     * Bisa diakses oleh siapa saja (tidak memerlukan otentikasi).
     * Backend hanya menghitung hash dan mengembalikannya ke frontend.
     * Frontend yang akan mengecek hash di blockchain.
     *
     * @param file File sertifikat yang akan diverifikasi.
     * @return ResponseEntity yang berisi hash dokumen.
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyCertificate(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(Collections.singletonMap("message", "File tidak boleh kosong!"), HttpStatus.BAD_REQUEST);
        }

        try {
            // Hitung hash SHA-256 dari file yang diunggah
            String documentHash = hashService.calculateSha256Hash(file.getInputStream());

            // Backend hanya mengembalikan hash. Frontend akan melanjutkan pengecekan di blockchain.
            return new ResponseEntity<>(Collections.singletonMap("documentHash", documentHash), HttpStatus.OK);

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algoritma hash tidak ditemukan: " + e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("message", "Error server: Algoritma hash tidak tersedia."), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Error umum saat memverifikasi sertifikat: " + e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("message", "Terjadi kesalahan yang tidak terduga."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint untuk mendapatkan daftar folder milik institusi dengan alamat blockchain tertentu.
     * @param userAddress Alamat MetaMask user/institusi.
     * @return List of folder names.
     */
    @GetMapping("/folders")
    public ResponseEntity<List<String>> getFolders(@RequestParam("userAddress") String userAddress) {
        if (userAddress == null || userAddress.trim().isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST);
        }

        try {
            List<String> folders = fileStorageService.getFolders(userAddress);
            return new ResponseEntity<>(folders, HttpStatus.OK);
        } catch (StorageException e) {
            System.err.println("Error saat mengambil daftar folder: " + e.getMessage());
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint untuk mendapatkan daftar file dan metadatanya di dalam folder tertentu milik institusi.
     * @param folderName Nama folder.
     * @param userAddress Alamat MetaMask user/institusi.
     * @return List of CertificateResponse objects for files in the specified folder.
     */
    @GetMapping("/folders/{folderName}/certificates")
    public ResponseEntity<List<CertificateResponse>> getCertificatesByFolder(
            @PathVariable String folderName,
            @RequestParam("userAddress") String userAddress) {
        if (userAddress == null || userAddress.trim().isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST);
        }

        try {
            List<Certificate> certificates = certificateService.getCertificatesByIssuerAddressAndFolder(userAddress, folderName);
            // Konversi entitas Certificate ke DTO CertificateResponse
            List<CertificateResponse> responseList = certificates.stream()
                    .map(cert -> CertificateResponse.builder()
                            .documentHash(cert.getDocumentHash())
                            .originalFileName(cert.getOriginalFileName())
                            .localFilePath(cert.getLocalFilePath())
                            .folderName(cert.getFolderName())
                            .issuerAddress(cert.getIssuerAddress())
                            .uploadTimestamp(cert.getUploadTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .build())
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error saat mengambil sertifikat di folder: " + e.getMessage());
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
