package com.authenticity.taranium_certs.service;


import com.authenticity.taranium_certs.exception.StorageException   ;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service untuk mengelola penyimpanan file secara lokal.
 * File disimpan dengan hierarki: root-upload-dir/email_user/nama_folder/nama_file.
 */
@Service
public class FileStorageService {

    private final Path rootLocation;

    /**
     * Konstruktor untuk menginisialisasi direktori root berdasarkan properti aplikasi.
     * @param uploadDir Lokasi direktori upload dari application.yml.
     */
    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Tidak dapat membuat direktori upload!", e);
        }
    }

    /**
     * Menyimpan file yang diunggah ke lokasi lokal yang ditentukan.
     * @param file File MultipartFile yang akan disimpan.
     * @param userEmail Email user (digunakan sebagai sub-direktori).
     * @param folderName Nama folder di bawah direktori user.
     * @return Path relatif dari file yang disimpan (dari rootLocation).
     * @throws StorageException jika terjadi kesalahan saat menyimpan file.
     */
    public String storeFile(MultipartFile file, String userEmail, String folderName) {
        if (file.isEmpty()) {
            throw new StorageException("Gagal menyimpan file kosong " + file.getOriginalFilename());
        }

        try {
            // Bersihkan nama file agar aman untuk filesystem
            String originalFileName = file.getOriginalFilename();
            String safeFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_"); // Hanya karakter aman

            Path userDir = this.rootLocation.resolve(userEmail);
            Files.createDirectories(userDir); // Buat direktori user jika belum ada

            Path folderDir = userDir.resolve(folderName);
            Files.createDirectories(folderDir); // Buat direktori folder jika belum ada

            Path destinationFile = folderDir.resolve(safeFileName);

            // Pastikan tidak menimpa file yang sudah ada kecuali memang itu yang diinginkan
            // Menggunakan StandardCopyOption.REPLACE_EXISTING akan menimpa.
            // Untuk proyek ini, kita anggap nama file unik di dalam folder.
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Mengembalikan path relatif dari rootLocation
            return this.rootLocation.relativize(destinationFile).toString();

        } catch (IOException e) {
            String msg = String.format("Gagal menyimpan file '%s' untuk user '%s' di folder '%s'.",
                    file.getOriginalFilename(), userEmail, folderName);
            throw new StorageException(msg, e);
        }
    }

    /**
     * Mengambil daftar folder (direktori tingkat pertama di bawah direktori user) untuk user tertentu.
     * @param userEmail Email user.
     * @return List of String yang berisi nama-nama folder.
     * @throws StorageException jika terjadi kesalahan saat membaca direktori.
     */
    public List<String> getFolders(String userEmail) {
        Path userDir = this.rootLocation.resolve(userEmail);
        if (!Files.exists(userDir) || !Files.isDirectory(userDir)) {
            return Collections.emptyList(); // Jika direktori user tidak ada, kembalikan list kosong
        }
        try (Stream<Path> paths = Files.list(userDir)) {
            return paths
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException("Gagal memuat folder untuk user: " + userEmail, e);
        }
    }

    /**
     * Mengambil daftar file di dalam folder tertentu untuk user tertentu.
     * @param userEmail Email user.
     * @param folderName Nama folder.
     * @return List of String yang berisi nama-nama file.
     * @throws StorageException jika terjadi kesalahan saat membaca direktori.
     */
    public List<String> getFiles(String userEmail, String folderName) {
        Path folderDir = this.rootLocation.resolve(userEmail).resolve(folderName);
        if (!Files.exists(folderDir) || !Files.isDirectory(folderDir)) {
            return Collections.emptyList(); // Jika direktori folder tidak ada, kembalikan list kosong
        }
        try (Stream<Path> paths = Files.list(folderDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException(String.format("Gagal memuat file di folder '%s' untuk user '%s'.", folderName, userEmail), e);
        }
    }
}
