package com.authenticity.taranium_certs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan; // Tetap ada jika ada @ConfigurationProperties di masa depan

/**
 * Kelas utama aplikasi Spring Boot.
 * Ini adalah titik masuk untuk menjalankan aplikasi backend.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TaraniumCertsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaraniumCertsApplication.class, args);
	}

}
