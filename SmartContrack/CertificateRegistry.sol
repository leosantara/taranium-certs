// SPDX-License-Identifier: MIT
// Lisensi yang menyatakan bahwa kode ini bersifat open-source di bawah lisensi MIT.
pragma solidity ^0.8.0; // Versi Solidity yang digunakan (kompatibel dengan 0.8.0 ke atas).

/**
 * @title CertificateRegistry
 * @dev Smart contract ini berfungsi untuk mendaftarkan dan memverifikasi hash kriptografi dari dokumen/sertifikat
 * di Taranium Smartchain. Ini menyediakan bukti keberadaan dan integritas yang tidak dapat diubah.
 */
contract CertificateRegistry {

    // Struktur data untuk menyimpan informasi dari setiap sertifikat yang terdaftar.
    // Ini akan menjadi "value" dari mapping 'certificates'.
    struct CertificateInfo {
        address issuer; // Alamat blockchain dari institusi/entitas yang menerbitkan/mengunggah sertifikat.
                        // Ini akan menjadi 'msg.sender' dari transaksi pendaftaran.
        uint256 timestamp; // Waktu (dalam Unix timestamp) saat sertifikat didaftarkan di blockchain.
        string metadataURI; // URI ke metadata off-chain (misalnya, hash IPFS atau URL ke deskripsi file).
                            // Bisa juga dikosongkan jika tidak ada metadata tambahan.
    }

    // Mapping ini adalah penyimpanan utama untuk data sertifikat.
    // Key: bytes32 (hash SHA-256 dokumen)
    // Value: CertificateInfo (struktur data yang menyimpan info penerbit, timestamp, dan metadataURI)
    mapping(bytes32 => CertificateInfo) public certificates;

    // Event yang dipancarkan setiap kali sertifikat baru berhasil didaftarkan.
    // Event ini penting untuk aplikasi off-chain (seperti frontend Anda) untuk memantau
    // dan mengindeks pendaftaran baru tanpa harus membaca seluruh state blockchain.
    event CertificateRegistered(
        bytes32 indexed documentHash, // Hash dokumen yang terdaftar (indexed agar mudah dicari di log)
        address indexed issuer,       // Alamat penerbit (indexed agar mudah dicari di log)
        uint256 timestamp             // Waktu pendaftaran
    );

    /**
     * @dev Fungsi untuk mendaftarkan hash dokumen baru ke blockchain.
     * Fungsi ini harus dipanggil oleh institusi/penerbit sertifikat.
     * Karena ini adalah transaksi yang mengubah state blockchain, ia akan membutuhkan gas fee.
     * @param _documentHash Hash SHA-256 dari dokumen yang akan didaftarkan. Harus unik.
     * @param _metadataURI URI opsional ke metadata off-chain terkait dokumen.
     */
    function registerDocument(bytes32 _documentHash, string calldata _metadataURI) public {
        // Memastikan bahwa hash dokumen ini belum pernah didaftarkan sebelumnya.
        // Jika certificates[_documentHash].issuer bukan address(0) (address nol), berarti sudah ada.
        require(certificates[_documentHash].issuer == address(0), "Certificate hash already registered.");

        // Menyimpan informasi sertifikat ke mapping 'certificates'.
        // 'msg.sender' adalah alamat yang memanggil fungsi ini (alamat wallet institusi).
        // 'block.timestamp' adalah waktu blok saat transaksi ini dieksekusi.
        certificates[_documentHash] = CertificateInfo(msg.sender, block.timestamp, _metadataURI);

        // Memancarkan event untuk memberitahu aplikasi off-chain tentang pendaftaran baru.
        emit CertificateRegistered(_documentHash, msg.sender, block.timestamp);
    }

    /**
     * @dev Fungsi untuk memeriksa apakah hash dokumen sudah terdaftar di blockchain.
     * Ini adalah fungsi 'view' (read-only), yang berarti tidak mengubah state blockchain
     * dan tidak memerlukan gas fee saat dipanggil dari off-chain (misalnya dari frontend).
     * @param _documentHash Hash SHA-256 dokumen yang akan dicek.
     * @return bool True jika hash terdaftar, false jika tidak.
     */
    function isDocumentRegistered(bytes32 _documentHash) public view returns (bool) {
        // Mengembalikan true jika 'issuer' dari CertificateInfo bukan address(0).
        // Jika address(0), berarti hash belum pernah didaftarkan.
        return certificates[_documentHash].issuer != address(0);
    }

    /**
     * @dev Fungsi untuk mendapatkan informasi lengkap tentang sertifikat yang terdaftar.
     * Ini juga fungsi 'view' dan tidak memerlukan gas fee.
     * @param _documentHash Hash SHA-256 dokumen.
     * @return issuer Alamat penerbit sertifikat.
     * @return timestamp Waktu pendaftaran sertifikat.
     * @return metadataURI URI metadata off-chain.
     */
    function getCertificateInfo(bytes32 _documentHash) public view returns (address issuer, uint256 timestamp, string memory metadataURI) {
        CertificateInfo storage info = certificates[_documentHash];
        // Memastikan hash terdaftar sebelum mengembalikan info.
        // Jika tidak, akan mengembalikan nilai default (0x0, 0, "")
        // Anda bisa tambahkan require(info.issuer != address(0), "Certificate not found.");
        // jika ingin ada error eksplisit saat hash tidak ditemukan.
        return (info.issuer, info.timestamp, info.metadataURI);
    }
}
