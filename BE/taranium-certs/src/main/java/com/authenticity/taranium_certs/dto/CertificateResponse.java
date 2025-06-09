package com.authenticity.taranium_certs.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO untuk merepresentasikan informasi sertifikat yang telah diupload.
 */
@Data // Lombok: otomatis membuat getter, setter, toString, equals, hashCode
@Builder // Lombok: otomatis membuat builder pattern
public class CertificateResponse {
    private String documentHash;
    private String originalFileName;
    private String localFilePath;
    private String folderName;
    private String issuerEmail;
    private String uploadTimestamp; // String untuk representasi tanggal/waktu yang mudah dibaca
    private String message; // Pesan sukses/error
}
