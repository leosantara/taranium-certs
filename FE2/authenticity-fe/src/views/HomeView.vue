<!-- src/views/HomeView.vue -->
<template>
  <main class="home-view">
    <h1 style="color: purple;">Verifikasi Sertifikat Digital</h1>
    <p style="color: purple;">Unggah sertifikat Anda untuk memeriksa keasliannya di Taranium Smartchain.</p>

    <div class="upload-section">
      <input type="file" @change="handleFileUpload" ref="fileInput" accept=".pdf,.jpg,.png" />
      <button @click="verifyCertificate" :disabled="!selectedFile || isVerifying">
        {{ isVerifying ? 'Memverifikasi...' : 'Verifikasi Sertifikat' }}
      </button>
    </div>

    <div v-if="verificationResult" class="result-section">
      <h3>Hasil Verifikasi:</h3>
      <div class="iniStatus"> 
        <p><strong>Hash Dokumen:</strong> {{ verificationResult.documentHash }}</p>
        <p><strong>Status Blockchain:</strong> <span style="color: red;">{{ blockchainStatus }}</span></p>
        <p v-if="blockchainStatus === 'ASLI dan TERDAFTAR'">
          Sertifikat ini ditemukan dan terdaftar di Taranium Smartchain!
          <span v-if="blockchainInfo.issuer"> Diterbitkan oleh: {{ blockchainInfo.issuer.substring(0, 8) }}...{{
            blockchainInfo.issuer.substring(blockchainInfo.issuer.length - 6) }}</span>
          <span v-if="blockchainInfo.timestamp"> pada: {{ blockchainInfo.timestamp }}</span>
        </p>
        <p v-else-if="blockchainStatus === 'TIDAK TERDAFTAR'">
          Sertifikat ini belum terdaftar di Taranium Smartchain.
        </p>
      </div>
      <p v-if="error" class="error-message">{{ error }}</p>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue';
import api from '../services/api';
import { checkHashOnChain } from '../utils/web3';

const selectedFile = ref(null);
const fileInput = ref(null);
const verificationResult = ref(null);
const blockchainStatus = ref('');
const blockchainInfo = ref({}); // Untuk menyimpan info dari blockchain (issuer, timestamp)
const isVerifying = ref(false);
const error = ref('');

const handleFileUpload = (event) => {
  selectedFile.value = event.target.files[0];
  verificationResult.value = null; // Reset hasil sebelumnya
  blockchainStatus.value = '';
  blockchainInfo.value = {};
  error.value = '';
};

const verifyCertificate = async () => {
  if (!selectedFile.value) {
    alert("Harap pilih file sertifikat terlebih dahulu.");
    return;
  }

  isVerifying.value = true;
  error.value = '';

  try {
    // Langkah 1: Kirim file ke backend untuk mendapatkan hash
    const formData = new FormData();
    formData.append('file', selectedFile.value);

    const backendResponse = await api.post('/api/verify', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    const documentHash = backendResponse.data.documentHash;
    verificationResult.value = { documentHash };

    // Langkah 2: Cek hash ini di blockchain Taranium
    const { isRegistered, info } = await checkHashOnChain(documentHash); // Panggil fungsi Web3.js
    if (isRegistered) {
      blockchainStatus.value = 'ASLI dan TERDAFTAR';
      blockchainInfo.value = info;
    } else {
      blockchainStatus.value = 'TIDAK TERDAFTAR';
      blockchainInfo.value = {};
    }

  } catch (err) {
    console.error("Gagal memverifikasi sertifikat:", err);
    error.value = `Terjadi kesalahan saat verifikasi: ${err.response?.data?.message || err.message}`;
    blockchainStatus.value = 'ERROR';
  } finally {
    isVerifying.value = false;
    if (fileInput.value) {
      fileInput.value.value = ''; // Reset input file
    }
    selectedFile.value = null;
  }
};
</script>

<style scoped>
.upload-section {
  color:#000000;
}
.result-section {
  color: #000000;
}
.result-section strong {
  font-weight: bold;
}
.home-view {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.upload-section {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.upload-section input[type="file"] {
  border: 1px solid #ccc;
  padding: 0.5rem;
  border-radius: 4px;
  width: 100%;
  max-width: 400px;
}

.upload-section button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1.1rem;
}

.upload-section button:hover:not(:disabled) {
  background-color: #36a076;
}

.upload-section button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.result-section {
  margin-top: 3rem;
  padding: 1.5rem;
  background-color: #e9f5e9;
  border: 1px solid #c8e6c9;
  border-radius: 8px;
  text-align: left;
}

.result-section h3 {
  color: #2196f3;
  margin-bottom: 1rem;
}

.result-section p {
  margin-bottom: 0.5rem;
}

.error-message {
  color: red;
  font-weight: bold;
}
</style>
