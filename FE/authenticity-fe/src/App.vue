    <!-- src/App.vue -->
    <template>
      <header>
        <div class="wrapper">
          <nav>
            <RouterLink to="/">Home (Verifikasi)</RouterLink>
            <RouterLink to="/dashboard" v-if="isLoggedIn">Dashboard (Institusi)</RouterLink>

            <!-- Tombol Login/Logout -->
            <div class="auth-section">
              <!-- Hapus 'prompt' dan 'auto-select' seperti yang kita diskusikan -->
              <GoogleLogin v-if="!isLoggedIn" :callback="handleGoogleLoginSuccess">
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
    import { GoogleLogin } from 'vue3-google-login';

    const isLoggedIn = ref(false); // Status login pengguna
    const router = useRouter();

    const checkLoginStatus = async () => {
      try {
        const response = await api.get('/api/auth/user'); // Endpoint untuk cek status login
        isLoggedIn.value = response.data.isAuthenticated && response.data.role === 'INSTITUTION';
      } catch (error) {
        console.error("Gagal memeriksa status login:", error);
        isLoggedIn.value = false;
      }
    };

    const handleGoogleLoginSuccess = async (response) => {
      console.log("Google Login Response:", response); // Debugging
      if (response.code) { // <--- PERUBAHAN DI SINI: Gunakan response.code
        try {
          // Kirim authorization code ke backend untuk penukaran token dan pembuatan sesi
          const backendAuthResponse = await api.post('/api/auth/google-auth-code', { // <--- PERUBAHAN: Endpoint baru
            code: response.code // <--- PERUBAHAN: Kirim 'code'
          });

          if (backendAuthResponse.data.isAuthenticated) {
            isLoggedIn.value = true;
            router.push('/dashboard');
          } else {
            alert("Login gagal di sisi server. Silakan coba lagi.");
            isLoggedIn.value = false;
          }
        } catch (error) {
          console.error("Error sending authorization code to backend:", error);
          alert(`Login gagal: ${error.response?.data?.message || error.message}`);
          isLoggedIn.value = false;
        }
      } else {
        alert("Gagal mendapatkan authorization code dari Google.");
      }
    };

    const handleLogout = async () => {
      try {
        await api.post('/api/auth/logout');
        isLoggedIn.value = false;
        router.push('/');
        alert("Anda telah berhasil logout.");
      } catch (error) {
        console.error("Gagal logout:", error);
        alert(`Logout gagal: ${error.response?.data?.message || error.message}`);
      }
    };

    onMounted(() => {
      checkLoginStatus();
    });
    </script>

    <style scoped>
    /* ... styling yang sama ... */
    </style>
    