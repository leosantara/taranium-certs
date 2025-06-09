package com.authenticity.taranium_certs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entitas Certificate yang merepresentasikan metadata dari sertifikat yang diupload.
 * Hash SHA-256 dokumen disimpan di sini, bersama dengan alamat blockchain pengunggah (institusi).
 */
@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String documentHash;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String localFilePath;

    @Column(nullable = false)
    private String folderName;

    @Column(nullable = false)
    private String issuerAddress; // Berubah: dari issuerEmail menjadi issuerAddress

    @Column(nullable = false)
    private LocalDateTime uploadTimestamp;

    // Konstruktor untuk membuat objek Certificate baru
    public Certificate(String documentHash, String originalFileName, String localFilePath, String folderName, String issuerAddress) {
        this.documentHash = documentHash;
        this.originalFileName = originalFileName;
        this.localFilePath = localFilePath;
        this.folderName = folderName;
        this.issuerAddress = issuerAddress;
        this.uploadTimestamp = LocalDateTime.now();
    }
}
