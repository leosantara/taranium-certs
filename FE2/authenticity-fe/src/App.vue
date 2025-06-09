<!-- src/App.vue -->
<template>
  <header>
    <div class="wrapper">
      <nav>
        <RouterLink to="/">Home (Verifikasi)</RouterLink>
        <RouterLink to="/dashboard" v-if="userAddress">Dashboard (Institusi)</RouterLink>

        <!-- Tombol Connect/Disconnect MetaMask -->
        <div class="auth-section">
          <button v-if="!userAddress" @click="handleConnectWallet" class="login-button">
            Connect MetaMask
          </button>
          <button v-else @click="handleDisconnectWallet" class="logout-button">
            Disconnect MetaMask ({{ displayAddress }})
          </button>
        </div>
      </nav>
    </div>
  </header>

  <!-- Pass userAddress sebagai prop ke RouterView untuk diakses oleh child component (DashboardView, dll) -->
  <RouterView :userAddress="userAddress" />
</template>

<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { ref, onMounted, computed } from 'vue';
import { connectWallet, getCurrentWalletAddress, setupWalletEventListeners } from './utils/web3';

const userAddress = ref(null); // Alamat MetaMask pengguna
const router = useRouter();

// Computed property untuk menampilkan alamat yang disingkat
const displayAddress = computed(() => {
  if (userAddress.value) {
    return `${userAddress.value.substring(0, 6)}...${userAddress.value.substring(userAddress.value.length - 4)}`;
  }
  return '';
});

// Fungsi untuk menghubungkan dompet MetaMask
const handleConnectWallet = async () => {
  const address = await connectWallet();
  if (address) {
    userAddress.value = address;
    router.push('/dashboard'); // Arahkan ke dashboard setelah koneksi berhasil
    alert("MetaMask terhubung!");
  }
};

// Fungsi untuk memutuskan dompet MetaMask (secara efektif, menghapus alamat dari state aplikasi)
const handleDisconnectWallet = () => {
  userAddress.value = null;
  // MetaMask tidak memiliki fungsi 'disconnect' langsung yang memutuskan koneksi
  // antara aplikasi dan dompet secara paksa. User harus melakukannya manual di MetaMask.
  // Ini hanya membersihkan state di sisi aplikasi.
  router.push('/'); // Arahkan kembali ke halaman utama
  alert("MetaMask terputus (di sisi aplikasi).");
};

// Fungsi untuk menangani perubahan akun dari MetaMask
const onAccountsChanged = (accounts) => {
  if (accounts.length === 0) {
    // User telah memutuskan koneksi semua akun dari MetaMask
    handleDisconnectWallet();
    alert("Semua akun MetaMask terputus dari aplikasi.");
  } else {
    // Akun berubah, update alamat
    userAddress.value = accounts[0];
    alert(`Akun MetaMask berubah ke: ${displayAddress.value}`);
  }
};

// Fungsi untuk menangani perubahan jaringan dari MetaMask
const onChainChanged = (chainId) => {
  console.log("Jaringan MetaMask berubah:", chainId);
  // Anda mungkin ingin memaksa user untuk berada di jaringan Taranium Testnet
  // Jika tidak, Anda bisa memberi peringatan atau memutuskan koneksi.
  alert("Jaringan MetaMask berubah. Harap pastikan di Taranium Smartchain Testnet.");
  // Periksa kembali alamat setelah perubahan jaringan
  getCurrentWalletAddress().then(address => {
    userAddress.value = address;
    if (address === null) {
      router.push('/'); // Jika tidak ada akun terhubung setelah chain change
    }
  });
};

// Cek status koneksi MetaMask saat komponen dimuat
onMounted(async () => {
  const address = await getCurrentWalletAddress();
  if (address) {
    userAddress.value = address;
  }
  // Setup event listener untuk MetaMask
  setupWalletEventListeners(onAccountsChanged, onChainChanged);
});
</script>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
  background-color: #f0f0f0;
  padding: 1rem;
  border-bottom: 1px solid #ddd;
}

.wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

nav {
  width: 100%;
  font-size: 1rem;
  text-align: center;
  display: flex;
  gap: 1rem;
  align-items: center;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-left: 1px solid var(--color-border);
  text-decoration: none;
  color: #333;
}

nav a:first-of-type {
  border: 0;
}

.auth-section {
  margin-left: auto; /* Dorong ke kanan */
}

.login-button, .logout-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
}

.login-button:hover, .logout-button:hover {
  background-color: #36a076;
}
</style>
