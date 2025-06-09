package com.authenticity.taranium_certs.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO untuk request upload sertifikat oleh institusi.
 * Mengandung file sertifikat dan nama folder.
 * Catatan: Ini DTO tidak secara langsung digunakan sebagai @RequestBody
 * karena file dan folderName diterima sebagai @RequestParam.
 * Namun, ini berguna untuk mendefinisikan struktur data.
 */
@Data
public class CertificateUploadRequest {
    private MultipartFile file;
    private String folderName;
}
