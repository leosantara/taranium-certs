package com.authenticity.taranium_certs.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO untuk merepresentasikan informasi sertifikat yang telah diupload.
 */
@Data
@Builder
public class CertificateResponse {
    private String documentHash;
    private String originalFileName;
    private String localFilePath;
    private String folderName;
    private String issuerAddress; // <--- Berubah: dari issuerEmail menjadi issuerAddress
    private String uploadTimestamp;
    private String message;
}
