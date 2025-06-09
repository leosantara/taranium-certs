package com.authenticity.taranium_certs.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Service untuk memverifikasi Google ID Token yang diterima dari frontend.
 */
@Service
public class GoogleIdTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;

    /**
     * Konstruktor untuk menginisialisasi GoogleIdTokenVerifier.
     * @param googleClientId Client ID Google dari application.yml.
     */
    public GoogleIdTokenVerifierService(@Value("${app.google.client-id}") String googleClientId) {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId)) // Ganti dengan Client ID aplikasi Anda
                .build();
    }

    /**
     * Memverifikasi Google ID Token.
     * @param idTokenString ID Token sebagai string.
     * @return GoogleIdToken jika token valid, null jika tidak valid.
     */
    public GoogleIdToken verify(String idTokenString) {
        try {
            return verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Gagal memverifikasi ID Token: " + e.getMessage());
            return null;
        }
    }
}
