package com.authenticity.taranium_certs.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfigurasi CORS (Cross-Origin Resource Sharing) untuk aplikasi.
 * Ini adalah cara lain untuk mengkonfigurasi CORS selain di application.yml.
 * Jika sudah dikonfigurasi di application.yml, ini bisa dihapus atau digunakan untuk aturan yang lebih kompleks.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Terapkan CORS ke semua endpoint
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000") // Izinkan frontend Anda
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Metode yang diizinkan
                .allowedHeaders("*") // Header yang diizinkan
                .allowCredentials(true); // Penting untuk mengirim cookie sesi
    }
}
