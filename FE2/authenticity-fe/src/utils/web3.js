// src/utils/web3.js
import { ethers } from 'ethers'; // Menggunakan ethers.js

// Konfigurasi Taranium Smartchain Testnet
const TARANIUM_RPC_URL = 'https://testnet-rpc.taranium.com'; // Ganti jika ada URL RPC resmi lainnya
const TARANIUM_CHAIN_ID = 9924; // Ganti dengan Chain ID Taranium Testnet yang sebenarnya

// ABI dan Contract Address dari Smart Contract CertificateRegistry Anda
// Anda harus mendapatkan ini setelah mengkompilasi dan mendeploy smart contract Anda di Remix IDE
const CERTIFICATE_REGISTRY_ABI = [
	{
		"anonymous": false,
		"inputs": [
			{
				"indexed": true,
				"internalType": "bytes32",
				"name": "documentHash",
				"type": "bytes32"
			},
			{
				"indexed": true,
				"internalType": "address",
				"name": "issuer",
				"type": "address"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "timestamp",
				"type": "uint256"
			}
		],
		"name": "CertificateRegistered",
		"type": "event"
	},
	{
		"inputs": [
			{
				"internalType": "bytes32",
				"name": "_documentHash",
				"type": "bytes32"
			},
			{
				"internalType": "string",
				"name": "_metadataURI",
				"type": "string"
			}
		],
		"name": "registerDocument",
		"outputs": [],
		"stateMutability": "nonpayable",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "bytes32",
				"name": "",
				"type": "bytes32"
			}
		],
		"name": "certificates",
		"outputs": [
			{
				"internalType": "address",
				"name": "issuer",
				"type": "address"
			},
			{
				"internalType": "uint256",
				"name": "timestamp",
				"type": "uint256"
			},
			{
				"internalType": "string",
				"name": "metadataURI",
				"type": "string"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "bytes32",
				"name": "_documentHash",
				"type": "bytes32"
			}
		],
		"name": "getCertificateInfo",
		"outputs": [
			{
				"internalType": "address",
				"name": "issuer",
				"type": "address"
			},
			{
				"internalType": "uint256",
				"name": "timestamp",
				"type": "uint256"
			},
			{
				"internalType": "string",
				"name": "metadataURI",
				"type": "string"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "bytes32",
				"name": "_documentHash",
				"type": "bytes32"
			}
		],
		"name": "isDocumentRegistered",
		"outputs": [
			{
				"internalType": "bool",
				"name": "",
				"type": "bool"
			}
		],
		"stateMutability": "view",
		"type": "function"
	}
];
const CERTIFICATE_REGISTRY_ADDRESS = "0xABd3Aa9a4ce3dDd822026c23E80D52a29bE64f11"; // Ganti dengan alamat kontrak Anda yang telah dideploy

// --- Fungsi untuk Manajemen Koneksi MetaMask ---

/**
 * Memastikan MetaMask terinstal dan terhubung ke Taranium Smartchain.
 * Mengembalikan alamat akun yang terhubung.
 * @returns {Promise<string|null>} Alamat akun MetaMask (string) jika berhasil, null jika gagal.
 */
export async function connectWallet() {
  if (window.ethereum) {
    try {
      // Meminta akses akun pengguna
      const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
      const provider = new ethers.BrowserProvider(window.ethereum);

      // Opsional: Cek apakah jaringan yang terhubung adalah Taranium Testnet
      const { chainId } = await provider.getNetwork();
      if (chainId !== BigInt(TARANIUM_CHAIN_ID)) { // Gunakan BigInt untuk perbandingan chainId
        alert(`Harap beralih ke Taranium Smartchain Testnet (Chain ID: ${TARANIUM_CHAIN_ID}) di MetaMask Anda.`);
        // Anda bisa tambahkan logika untuk meminta pengguna beralih jaringan di MetaMask secara otomatis
        try {
          await window.ethereum.request({
            method: 'wallet_addEthereumChain',
            params: [{
              chainId: '0x' + TARANIUM_CHAIN_ID.toString(16), // Chain ID dalam heksadesimal
              rpcUrls: [TARANIUM_RPC_URL],
              chainName: 'Taranium Smartchain Testnet',
              nativeCurrency: { name: 'TARAN', symbol: 'TARAN', decimals: 18 }, // Ganti dengan detail token Taranium
              blockExplorerUrls: ['https://testnet-scan.taranium.com/']
            }]
          });
          // Setelah menambah/beralih, coba lagi request accounts
          const updatedAccounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
          if (updatedAccounts.length > 0) {
            return updatedAccounts[0];
          } else {
            return null;
          }
        } catch (addError) {
          console.error("Gagal menambah/beralih jaringan:", addError);
          return null;
        }
      }

      if (accounts.length > 0) {
        return accounts[0]; // Mengembalikan alamat akun pertama
      } else {
        alert("Tidak ada akun MetaMask yang terhubung.");
        return null;
      }
    } catch (error) {
      console.error("Kesalahan saat menghubungkan ke dompet:", error);
      alert("Gagal terhubung ke dompet. Pastikan MetaMask terinstal dan akun Anda aktif.");
      return null;
    }
  } else {
    alert("Dompet web3 (misalnya MetaMask) tidak terinstal. Silakan instal untuk melanjutkan.");
    return null;
  }
}

/**
 * Mendapatkan alamat akun MetaMask yang saat ini terhubung.
 * @returns {Promise<string|null>} Alamat akun MetaMask (string) jika terhubung, null jika tidak.
 */
export async function getCurrentWalletAddress() {
  if (window.ethereum) {
    try {
      const provider = new ethers.BrowserProvider(window.ethereum);
      const accounts = await provider.listAccounts(); // Mendapatkan daftar akun yang terhubung
      if (accounts.length > 0) {
        return accounts[0].address; // Mengembalikan alamat akun pertama
      }
      return null;
    } catch (error) {
      console.error("Gagal mendapatkan akun yang terhubung:", error);
      return null;
    }
  }
  return null;
}

/**
 * Menyiapkan event listener untuk perubahan akun dan jaringan di MetaMask.
 * @param {function} onAccountsChanged Callback saat akun berubah.
 * @param {function} onChainChanged Callback saat jaringan berubah.
 */
export function setupWalletEventListeners(onAccountsChanged, onChainChanged) {
  if (window.ethereum) {
    window.ethereum.on('accountsChanged', (accounts) => {
      console.log("MetaMask accounts changed:", accounts);
      onAccountsChanged(accounts);
    });
    window.ethereum.on('chainChanged', (chainId) => {
      console.log("MetaMask chain changed:", chainId);
      onChainChanged(chainId);
    });
    // handle disconnect if needed
    // window.ethereum.on('disconnect', (error) => { /* ... */ });
  }
}

// --- Fungsi Interaksi Smart Contract ---

/**
 * Mendaftarkan hash dokumen ke smart contract di Taranium Smartchain.
 * @param {string} documentHash Hash SHA-256 dari dokumen.
 * @param {string} metadataURI URI metadata jika ada (misal, link ke IPFS untuk detail).
 * @returns {Promise<string|null>} Transaction hash jika berhasil, null jika gagal.
 */
export async function registerHashOnChain(documentHash, metadataURI = "") {
  const signer = await new ethers.BrowserProvider(window.ethereum).getSigner(); // Dapatkan signer dari provider yang terhubung
  if (!signer) {
    alert("Gagal mendapatkan signer. Pastikan dompet terhubung.");
    return null;
  }

  try {
    const contract = new ethers.Contract(CERTIFICATE_REGISTRY_ADDRESS, CERTIFICATE_REGISTRY_ABI, signer);
    // Pastikan documentHash diformat sebagai bytes32
    // Jika documentHash dari backend adalah string heksadesimal 64 karakter,
    // ethers.utils.arrayify(`0x${documentHash}`) bisa digunakan, tapi cukup string 0x
    const formattedDocumentHash = `0x${documentHash}`; // Tambahkan 0x jika belum ada

    const tx = await contract.registerDocument(formattedDocumentHash, metadataURI);
    await tx.wait(); // Tunggu hingga transaksi dikonfirmasi
    console.log("Transaksi pendaftaran hash berhasil:", tx.hash);
    return tx.hash;
  } catch (error) {
    console.error("Gagal mendaftarkan hash ke blockchain:", error);
    alert(`Gagal mendaftarkan hash ke blockchain: ${error.message || error.code || 'Unknown error'}`);
    return null;
  }
}

/**
 * Mengecek keberadaan hash dokumen di smart contract Taranium Smartchain.
 * Untuk user umum (tidak perlu login, hanya perlu wallet terhubung jika perlu).
 * @param {string} documentHash Hash SHA-256 dari dokumen yang akan dicek.
 * @returns {Promise<{isRegistered: boolean, info: object|null}>} Objek berisi status terdaftar dan info jika ada.
 */
export async function checkHashOnChain(documentHash) {
  let provider;
  if (window.ethereum) {
    provider = new ethers.BrowserProvider(window.ethereum);
  } else {
    // Fallback ke JsonRpcProvider jika MetaMask tidak terinstal, hanya untuk fungsi view
    provider = new ethers.JsonRpcProvider(TARANIUM_RPC_URL);
  }

  try {
    const contract = new ethers.Contract(CERTIFICATE_REGISTRY_ADDRESS, CERTIFICATE_REGISTRY_ABI, provider);
    const formattedDocumentHash = `0x${documentHash}`;

    // Cek apakah hash terdaftar
    const isRegistered = await contract.isDocumentRegistered(formattedDocumentHash);

    let info = null;
    if (isRegistered) {
      // Jika terdaftar, ambil info lengkap
      const [issuer, timestamp, metadataURI] = await contract.getCertificateInfo(formattedDocumentHash);
      info = {
        issuer: issuer,
        timestamp: new Date(Number(timestamp) * 1000).toLocaleString(), // Konversi timestamp ke tanggal yang mudah dibaca
        metadataURI: metadataURI
      };
    }
    console.log(`Hash ${documentHash} terdaftar: ${isRegistered}`, info);
    return { isRegistered, info };
  } catch (error) {
    console.error("Gagal mengecek hash di blockchain:", error);
    // Jika terjadi error, asumsikan tidak terdaftar untuk keamanan
    return { isRegistered: false, info: null };
  }
}
