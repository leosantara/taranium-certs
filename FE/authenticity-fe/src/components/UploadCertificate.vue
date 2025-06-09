<template>
  <div class="upload-container">
    <h2>Upload Sertifikat Baru</h2>
    <p>Unggah sertifikat digital Anda di sini. File akan di-hash dan disimpan secara lokal di backend, kemudian Anda dapat mendaftarkan hash ini di Taranium Smartchain.</p>

    <div class="form-group">
      <label for="file">Pilih File Sertifikat:</label>
      <input type="file" id="file" @change="handleFileUpload" ref="fileInput" accept=".pdf,.jpg,.png" />
    </div>

    <div class="form-group">
      <label for="folderName">Nama Folder (misal: "Ijazah-2023", "SertifikatPelatihan"):</label>
      <input type="text" id="folderName" v-model="folderName" placeholder="Masukkan nama folder" />
    </div>

    <button @click="uploadAndHash" :disabled="!selectedFile || !folderName || isUploading">
      {{ isUploading ? 'Mengunggah & Menghitung Hash...' : 'Unggah & Hitung Hash' }}
    </button>

    <div v-if="uploadResult" class="result-box">
      <h3>Hasil Upload & Hash:</h3>
      <p><strong>Status:</strong> {{ uploadResult.message }}</p>
      <p><strong>Hash Dokumen:</strong> {{ uploadResult.documentHash }}</p>
      <p><strong>Path Lokal:</strong> {{ uploadResult.filePath }}</p>
      <p v-if="error" class="error-message">{{ error }}</p>

      <button @click="registerHash" :disabled="!uploadResult.documentHash || isRegistering" class="register-button">
        {{ isRegistering ? 'Mendaftarkan ke Blockchain...' : 'Daftarkan Hash ke Blockchain' }}
      </button>
      <p v-if="registrationTxHash" class="success-message">
        Transaksi Pendaftaran Berhasil! Tx Hash: <a :href="`https://testnet-scan.taranium.com/tx/${registrationTxHash}`" target="_blank">{{ registrationTxHash.substring(0, 10) }}...</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import api from '../services/api';
import { registerHashOnChain } from '../utils/web3';

const selectedFile = ref(null);
const fileInput = ref(null);
const folderName = ref('');
const uploadResult = ref(null);
const isUploading = ref(false);
const isRegistering = ref(false);
const error = ref('');
const registrationTxHash = ref(null);

const handleFileUpload = (event) => {
  selectedFile.value = event.target.files[0];
  uploadResult.value = null; // Reset hasil sebelumnya
  error.value = '';
  registrationTxHash.value = null;
};

const uploadAndHash = async () => {
  if (!selectedFile.value) {
    alert("Harap pilih file sertifikat terlebih dahulu.");
    return;
  }
  if (!folderName.value.trim()) {
    alert("Nama folder tidak boleh kosong.");
    return;
  }

  isUploading.value = true;
  error.value = '';

  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    formData.append('folderName', folderName.value);

    const response = await api.post('/api/register', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    uploadResult.value = response.data;

  } catch (err) {
    console.error("Gagal mengunggah atau menghitung hash:", err);
    error.value = `Terjadi kesalahan: ${err.response?.data?.message || err.message}`;
  } finally {
    isUploading.value = false;
  }
};

const registerHash = async () => {
  if (!uploadResult.value || !uploadResult.value.documentHash) {
    alert("Harap unggah dan hitung hash terlebih dahulu.");
    return;
  }

  isRegistering.value = true;
  error.value = '';

  try {
    // Untuk tujuan demo, kita akan gunakan alamat dompet user yang terhubung sebagai ownerAddress.
    // Dalam aplikasi nyata, Anda mungkin perlu input manual atau logika untuk menentukan pemilik sertifikat sebenarnya (misal: mahasiswa).
    // Atau smart contract Anda tidak perlu ownerAddress dan hanya mencatat hash dan issuer.
    // Untuk POC, kita akan asumsikan ownerAddress adalah alamat signer (institusi).
    const signer = await connectWallet(); // Meminta koneksi ke dompet (misal MetaMask)
    if (!signer) {
        throw new Error("Gagal terhubung ke dompet. Pastikan MetaMask terinstal dan terhubung.");
    }
    const ownerAddress = await signer.getAddress(); // Alamat dompet institusi yang menandatangani transaksi

    const txHash = await registerHashOnChain(uploadResult.value.documentHash, ownerAddress, ""); // metadataURI dikosongkan dulu

    if (txHash) {
      registrationTxHash.value = txHash;
      alert("Hash berhasil didaftarkan ke blockchain!");
    } else {
      alert("Pendaftaran hash ke blockchain gagal.");
    }

  } catch (err) {
    console.error("Gagal mendaftarkan hash ke blockchain:", err);
    error.value = `Terjadi kesalahan saat mendaftar ke blockchain: ${err.message}`;
  } finally {
    isRegistering.value = false;
    if (fileInput.value) {
      fileInput.value.value = ''; // Reset input file
    }
    selectedFile.value = null;
    folderName.value = ''; // Reset nama folder
  }
};
</script>

<style scoped>
.upload-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 1.5rem;
}

p {
  text-align: center;
  color: #555;
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #333;
}

.form-group input[type="file"],
.form-group input[type="text"] {
  width: calc(100% - 1rem);
  padding: 0.8rem;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 1rem;
}

button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1.1rem;
  width: 100%;
  margin-top: 1rem;
}

button:hover:not(:disabled) {
  background-color: #36a076;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.result-box {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #e9f5e9;
  border: 1px solid #c8e6c9;
  border-radius: 8px;
  text-align: left;
}

.result-box h3 {
  color: #2196f3;
  margin-bottom: 1rem;
}

.result-box p {
  text-align: left;
  margin-bottom: 0.5rem;
}

.register-button {
  background-color: #007bff;
  margin-top: 1rem;
}

.register-button:hover:not(:disabled) {
  background-color: #0056b3;
}

.error-message {
  color: red;
  font-weight: bold;
  margin-top: 1rem;
  text-align: center;
}

.success-message {
  color: green;
  font-weight: bold;
  margin-top: 1rem;
  text-align: center;
}
</style>