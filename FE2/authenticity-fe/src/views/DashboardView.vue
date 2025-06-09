<!-- src/views/DashboardView.vue -->
<template>
  <main class="dashboard-view">
    <h1 style="color: purple;">Dashboard Institusi</h1>
    <p style="color: purple;" v-if="userAddress">Selamat datang, Institusi: {{ displayAddress }}!</p>
    <p style="color: purple;" v-else class="warning-message">Anda harus terhubung ke MetaMask untuk melihat dashboard ini.</p>

    <div v-if="userAddress" class="dashboard-sections">
      <section class="upload-section">
        <h3>Upload Sertifikat Baru</h3>
        <UploadCertificate :userAddress="userAddress" />
      </section>

      <section class="folder-section">
        <h3>Dokumen yang Telah Diunggah</h3>
        <button @click="fetchFolders" :disabled="isLoadingFolders">
          {{ isLoadingFolders ? 'Memuat...' : 'Muat Folder Saya' }}
        </button>

        <div v-if="folders.length > 0">
          <h4>Folder:</h4>
          <ul>
            <li style="color: red;" v-for="folder in folders" :key="folder" @click="selectFolder(folder)" :class="{ 'selected': selectedFolder === folder }">
              {{ folder }}
            </li>
          </ul>
        </div>
        <p v-else-if="!isLoadingFolders" style="color: black;">Belum ada folder yang diunggah.</p>
        <p v-if="folderError" class="error-message">{{ folderError }}</p>

        <div v-if="selectedFolder" class="files-section">
          <h4 style="color: black;">Sertifikat di Folder "<strong>{{ selectedFolder }}</strong>":</h4>
          <ul v-if="filesInSelectedFolder.length > 0">
            <li v-for="cert in filesInSelectedFolder" :key="cert.documentHash" class="certificate-item">
              <strong>File:</strong> {{ cert.originalFileName }}<br>
              <strong>Hash:</strong> {{ cert.documentHash.substring(0, 10) }}...<br>
              <strong>Diunggah:</strong> {{ cert.uploadTimestamp }}<br>
              <strong>Path Lokal:</strong> {{ cert.localFilePath }}
            </li>
          </ul>
          <p v-else-if="!isLoadingFiles">Tidak ada sertifikat di folder ini.</p>
          <p v-if="fileError" class="error-message">{{ fileError }}</p>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import api from '../services/api';
import UploadCertificate from '../components/UploadCertificate.vue';

const props = defineProps({
  userAddress: {
    type: String,
    default: null
  }
});

const folders = ref([]);
const selectedFolder = ref(null);
const filesInSelectedFolder = ref([]); // Sekarang akan menyimpan objek CertificateResponse
const isLoadingFolders = ref(false);
const isLoadingFiles = ref(false);
const folderError = ref('');
const fileError = ref('');

// Computed property untuk menampilkan alamat yang disingkat
const displayAddress = computed(() => {
  if (props.userAddress) {
    return `${props.userAddress.substring(0, 6)}...${props.userAddress.substring(props.userAddress.length - 4)}`;
  }
  return '';
});

// Fungsi untuk mengambil daftar folder dari backend
const fetchFolders = async () => {
  if (!props.userAddress) {
    folderError.value = "Hubungkan MetaMask untuk memuat folder.";
    return;
  }
  isLoadingFolders.value = true;
  folderError.value = '';
  try {
    const response = await api.get('/api/folders', {
      params: { userAddress: props.userAddress } // Kirim alamat MetaMask sebagai parameter
    });
    folders.value = response.data;
    // Jika ada folder dan belum ada yang terpilih, pilih yang pertama
    if (folders.value.length > 0 && !selectedFolder.value) {
      selectedFolder.value = folders.value[0];
    }
  } catch (err) {
    console.error("Gagal memuat folder:", err);
    folderError.value = `Gagal memuat folder: ${err.response?.data?.message || err.message}`;
  } finally {
    isLoadingFolders.value = false;
  }
};

// Fungsi untuk mengambil daftar file/sertifikat di folder yang dipilih
const fetchFilesInFolder = async (folder) => {
  if (!folder || !props.userAddress) {
    return;
  }
  isLoadingFiles.value = true;
  fileError.value = '';
  try {
    const response = await api.get(`/api/folders/${folder}/certificates`, {
      params: { userAddress: props.userAddress } // Kirim alamat MetaMask sebagai parameter
    });
    filesInSelectedFolder.value = response.data; // Respons kini adalah List<CertificateResponse>
  } catch (err) {
    console.error(`Gagal memuat sertifikat di folder ${folder}:`, err);
    fileError.value = `Gagal memuat sertifikat: ${err.response?.data?.message || err.message}`;
  } finally {
    isLoadingFiles.value = false;
  }
};

const selectFolder = (folder) => {
  selectedFolder.value = folder;
};

// Watcher untuk memuat file ketika folder yang dipilih atau userAddress berubah
watch(() => props.userAddress, (newAddress) => {
  if (newAddress) {
    fetchFolders(); // Muat ulang folder saat userAddress tersedia
  } else {
    // Jika userAddress hilang, reset semua
    folders.value = [];
    selectedFolder.value = null;
    filesInSelectedFolder.value = [];
  }
});

watch(selectedFolder, (newFolder) => {
  if (newFolder) {
    fetchFilesInFolder(newFolder);
  } else {
    filesInSelectedFolder.value = [];
  }
});

onMounted(() => {
  // Jika userAddress sudah ada saat mount (misal dari refresh halaman)
  if (props.userAddress) {
    fetchFolders();
  }
});
</script>

<style scoped>
.certificate-item {
  color: #4CAF50;
}
.dashboard-view {
  max-width: 1000px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1, p {
  text-align: center;
  margin-bottom: 1rem;
}

.warning-message {
  color: #d9534f;
  font-weight: bold;
  text-align: center;
}

.dashboard-sections {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
  margin-top: 2rem;
}

@media (min-width: 768px) {
  .dashboard-sections {
    grid-template-columns: 1fr 1fr;
  }
}

section {
  padding: 1.5rem;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #f9f9f9;
}

h3 {
  color: #2196f3;
  margin-bottom: 1rem;
}

.folder-section ul, .files-section ul {
  list-style: none;
  padding: 0;
}

.folder-section li, .files-section li {
  background-color: #e0f2f7;
  margin-bottom: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.folder-section li:hover:not(.selected) {
  background-color: #cce9f5;
}

.folder-section li.selected {
  background-color: #a7d9ee;
  font-weight: bold;
  border: 1px solid #2196f3;
}

.certificate-item {
    background-color: #f0fff0; /* Warna lebih hijau untuk item sertifikat */
    border: 1px solid #c8e6c9;
    padding: 1rem;
    margin-bottom: 0.8rem;
    border-radius: 5px;
    font-size: 0.9em;
}
.certificate-item strong {
    color: #4CAF50; /* Warna hijau untuk label */
}

.error-message {
  color: red;
  font-weight: bold;
  text-align: center;
  margin-top: 1rem;
}
</style>
