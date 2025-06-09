package com.authenticity.taranium_certs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Kelas utama aplikasi Spring Boot.
 * Ini adalah titik masuk untuk menjalankan aplikasi backend.
 * @ConfigurationPropertiesScan diperlukan jika Anda menggunakan @ConfigurationProperties
 * untuk mengikat properti kustom dari application.yml ke kelas Java.
 */
@SpringBootApplication
@ConfigurationPropertiesScan // Pindai kelas dengan anotasi @ConfigurationProperties
public class TaraniumCertsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaraniumCertsApplication.class, args);
	}

}
