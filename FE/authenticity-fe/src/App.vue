<!-- src/App.vue -->
<template>
  <header>
    <div class="wrapper">
      <nav>
        <RouterLink to="/">Home (Verifikasi)</RouterLink>
        <RouterLink to="/dashboard" v-if="isLoggedIn">Dashboard (Institusi)</RouterLink>

        <!-- Tombol Login/Logout -->
        <div class="auth-section">
<GoogleLogin
  v-if="!isLoggedIn"
  :callback="handleGoogleLoginSuccess"
>
  <button class="login-button">Login Institusi</button>
</GoogleLogin>

          <button v-else @click="handleLogout" class="logout-button">Logout</button>
        </div>
      </nav>
    </div>
  </header>

  <RouterView />
</template>

<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { ref, onMounted } from 'vue';
import api from './services/api';
import { GoogleLogin } from 'vue3-google-login'; // Impor komponen GoogleLogin

const isLoggedIn = ref(false); // Status login pengguna
const router = useRouter();

// Fungsi untuk memeriksa status login dari backend
const checkLoginStatus = async () => {
  try {
    const response = await api.get('/api/user'); // Endpoint baru untuk cek status login
    isLoggedIn.value = response.data.isAuthenticated && response.data.role === 'INSTITUTION';
  } catch (error) {
    console.error("Gagal memeriksa status login:", error);
    isLoggedIn.value = false;
  }
};

// Callback setelah Google Login berhasil dari frontend
const handleGoogleLoginSuccess = async (response) => {
  if (response.credential) {
    try {
      // Kirim id_token ke backend untuk verifikasi dan pembuatan sesi
      const backendAuthResponse = await api.post('/api/auth/google-login', {
        idToken: response.credential
      });

      if (backendAuthResponse.data.isAuthenticated) {
        isLoggedIn.value = true;
        router.push('/dashboard'); // Redirect ke dashboard setelah login berhasil
      } else {
        alert("Login gagal di sisi server. Silakan coba lagi.");
        isLoggedIn.value = false;
      }
    } catch (error) {
      console.error("Error sending id_token to backend:", error);
      alert(`Login gagal: ${error.response?.data?.message || error.message}`);
      isLoggedIn.value = false;
    }
  }
};

// Handle logout
const handleLogout = async () => {
  try {
    await api.post('/api/auth/logout'); // Endpoint logout custom di backend
    isLoggedIn.value = false;
    router.push('/'); // Redirect ke halaman utama setelah logout
    alert("Anda telah berhasil logout.");
  } catch (error) {
    console.error("Gagal logout:", error);
    alert(`Logout gagal: ${error.response?.data?.message || error.message}`);
  }
};

// Cek status login saat komponen dimuat
onMounted(() => {
  checkLoginStatus();
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
