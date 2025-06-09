package com.authenticity.taranium_certs.controller;

// src/main/java/com/taraniumcerts/controller/CertificateController.java

import com.authenticity.taranium_certs.dto.CertificateResponse;
import com.authenticity.taranium_certs.dto.FolderContentResponse;
import com.authenticity.taranium_certs.entity.AppUser; // Import AppUser
import com.authenticity.taranium_certs.entity.Certificate;
import com.authenticity.taranium_certs.exception.StorageException;
import com.authenticity.taranium_certs.service.*;
import com.authenticity.taranium_certs.service.UserService; // Import UserService
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller untuk mengelola operasi sertifikat: pendaftaran dan verifikasi.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class CertificateController {

    private final HashService hashService;
    private final FileStorageService fileStorageService;
    private final CertificateService certificateService;
    private final UserService userService; // Inject UserService

    public CertificateController(HashService hashService, FileStorageService fileStorageService, CertificateService certificateService, UserService userService) {
        this.hashService = hashService;
        this.fileStorageService = fileStorageService;
        this.certificateService = certificateService;
        this.userService = userService;
    }

    /**
     * Endpoint untuk mendaftarkan hash sertifikat dan menyimpannya secara lokal.
     * Hanya bisa diakses oleh user dengan role INSTITUTION (setelah login Google).
     *
     * @param file File sertifikat yang diunggah.
     * @param folderName Nama folder tempat file akan disimpan lokal (misal: "Ijazah-2023").
     * @param authentication Objek Authentication dari Spring Security.
     * @return ResponseEntity yang berisi hash, path file lokal, dan pesan.
     */
    @PostMapping("/register")
    public ResponseEntity<CertificateResponse> registerCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderName") String folderName,
            Authentication authentication) {

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

        // Pastikan user terotentikasi dan memiliki role INSTITUTION
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("Akses ditolak. Anda harus login sebagai institusi.").build(),
                    HttpStatus.UNAUTHORIZED
            );
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // Dapatkan email user sebagai issuerId dari UserDetails
        String issuerEmail = userDetails.getUsername();

        // Cek apakah user memiliki role INSTITUTION
        boolean hasInstitutionRole = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION"));

        if (!hasInstitutionRole) {
            return new ResponseEntity<>(
                    CertificateResponse.builder().message("Akses ditolak. Anda tidak memiliki izin sebagai institusi.").build(),
                    HttpStatus.FORBIDDEN
            );
        }

        try {
            // 1. Hitung hash SHA-256 dari file yang diunggah
            String documentHash = hashService.calculateSha256Hash(file.getInputStream());

            // 2. Simpan file secara lokal
            String localFilePath = fileStorageService.storeFile(file, issuerEmail, folderName);

            // 3. Simpan metadata sertifikat ke database lokal
            Certificate certificate = new Certificate(documentHash, file.getOriginalFilename(), localFilePath, folderName, issuerEmail);
            certificateService.saveCertificateMetadata(certificate);

            // Backend hanya mengembalikan hash dan path. Frontend akan melanjutkan interaksi ke blockchain.
            return new ResponseEntity<>(
                    CertificateResponse.builder()
                            .documentHash(documentHash)
                            .originalFileName(file.getOriginalFilename())
                            .localFilePath(localFilePath)
                            .folderName(folderName)
                            .issuerEmail(issuerEmail)
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
     * Endpoint untuk mendapatkan daftar folder milik institusi yang login.
     * @param authentication Objek Authentication dari Spring Security.
     * @return List of folder names.
     */
    @GetMapping("/folders")
    public ResponseEntity<List<String>> getFolders(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        try {
            List<String> folders = fileStorageService.getFolders(userEmail);
            return new ResponseEntity<>(folders, HttpStatus.OK);
        } catch (StorageException e) {
            System.err.println("Error saat mengambil daftar folder: " + e.getMessage());
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint untuk mendapatkan daftar file dan metadatanya di dalam folder tertentu milik institusi yang login.
     * @param folderName Nama folder.
     * @param authentication Objek Authentication dari Spring Security.
     * @return List of CertificateResponse objects for files in the specified folder.
     */
    @GetMapping("/folders/{folderName}/certificates")
    public ResponseEntity<List<CertificateResponse>> getCertificatesByFolder(
            @PathVariable String folderName,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String issuerEmail = userDetails.getUsername();

        try {
            List<Certificate> certificates = certificateService.getCertificatesByIssuerEmailAndFolder(issuerEmail, folderName);
            // Konversi entitas Certificate ke DTO CertificateResponse
            List<CertificateResponse> responseList = certificates.stream()
                    .map(cert -> CertificateResponse.builder()
                            .documentHash(cert.getDocumentHash())
                            .originalFileName(cert.getOriginalFileName())
                            .localFilePath(cert.getLocalFilePath())
                            .folderName(cert.getFolderName())
                            .issuerEmail(cert.getIssuerEmail())
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
