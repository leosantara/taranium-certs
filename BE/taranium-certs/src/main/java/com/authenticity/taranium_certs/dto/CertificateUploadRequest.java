package com.authenticity.taranium_certs.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO untuk request upload sertifikat oleh institusi.
 * Mengandung file sertifikat dan nama folder.
 */
@Data // Lombok: otomatis membuat getter, setter, toString, equals, hashCode
public class CertificateUploadRequest {
    private MultipartFile file;
    private String folderName;
}
