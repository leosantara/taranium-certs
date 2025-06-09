package com.authenticity.taranium_certs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entitas Certificate yang merepresentasikan metadata dari sertifikat yang diupload.
 * Hash SHA-256 dokumen disimpan di sini, bersama dengan informasi pengunggah (institusi).
 */
@Entity
@Table(name = "certificates") // Nama tabel di database
@NoArgsConstructor // Lombok: otomatis membuat konstruktor tanpa argumen
@AllArgsConstructor // Lombok: otomatis membuat konstruktor dengan semua argumen
public class Certificate {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Strategi auto-increment ID
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, nullable = false, length = 64) // Hash SHA-256, panjang 64 karakter heksadesimal
    private String documentHash;

    @Getter
    @Setter
    @Column(nullable = false)
    private String originalFileName; // Nama file asli saat diupload

    @Getter
    @Setter
    @Column(nullable = false)
    private String localFilePath; // Path relatif tempat file disimpan di server

    @Getter
    @Setter
    @Column(nullable = false)
    private String folderName; // Nama folder yang ditentukan institusi

    @Getter
    @Setter
    @Column(nullable = false)
    private String issuerEmail; // Email institusi yang mengupload sertifikat

    @Getter
    @Setter
    @Column(nullable = false)
    private LocalDateTime uploadTimestamp; // Waktu upload

    // Konstruktor untuk membuat objek Certificate baru
    public Certificate(String documentHash, String originalFileName, String localFilePath, String folderName, String issuerEmail) {
        this.documentHash = documentHash;
        this.originalFileName = originalFileName;
        this.localFilePath = localFilePath;
        this.folderName = folderName;
        this.issuerEmail = issuerEmail;
        this.uploadTimestamp = LocalDateTime.now(); // Set waktu saat ini
    }
}
