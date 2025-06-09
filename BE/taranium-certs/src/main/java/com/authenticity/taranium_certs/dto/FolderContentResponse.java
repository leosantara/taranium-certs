package com.authenticity.taranium_certs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO untuk merepresentasikan konten folder (daftar file di dalamnya).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderContentResponse {
    private String folderName;
    private List<String> fileNames;
}
