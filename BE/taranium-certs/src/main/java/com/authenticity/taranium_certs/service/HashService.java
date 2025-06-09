package com.authenticity.taranium_certs.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service untuk menghitung hash SHA-256 dari sebuah InputStream (file).
 */
@Service
public class HashService {

    /**
     * Menghitung hash SHA-256 dari data yang dibaca dari InputStream.
     * @param inputStream InputStream dari file yang akan di-hash.
     * @return String representasi heksadesimal dari hash SHA-256.
     * @throws IOException jika terjadi kesalahan saat membaca InputStream.
     * @throws NoSuchAlgorithmException jika algoritma SHA-256 tidak tersedia.
     */
    public String calculateSha256Hash(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192]; // Buffer untuk membaca file
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        byte[] hashedBytes = digest.digest();

        // Konversi byte array ke string heksadesimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
