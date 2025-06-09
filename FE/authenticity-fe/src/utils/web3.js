// src/utils/web3.js
import { ethers } from 'ethers'; // Menggunakan ethers.js, lebih modern dan sering digunakan

// Konfigurasi Taranium Smartchain Testnet
const TARANIUM_RPC_URL = 'https://testnet-rpc.taranium.com'; // Ganti jika ada URL RPC resmi lainnya
const TARANIUM_CHAIN_ID = 2024; // Ganti dengan Chain ID Taranium Testnet yang sebenarnya

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
	}
];
const CERTIFICATE_REGISTRY_ADDRESS = "0xABd3Aa9a4ce3dDd822026c23E80D52a29bE64f11"; // Ganti dengan alamat kontrak Anda yang telah dideploy

/**
 * Menghubungkan ke dompet (misalnya MetaMask) dan mendapatkan provider/signer.
 * @returns {Promise<ethers.Signer|null>} Objek Signer jika berhasil terhubung, null jika gagal.
 */
export async function connectWallet() {
  if (window.ethereum) {
    try {
      // Meminta akses akun pengguna
      await window.ethereum.request({ method: 'eth_requestAccounts' });
      const provider = new ethers.BrowserProvider(window.ethereum);
      const signer = await provider.getSigner();

      // Opsional: Cek apakah jaringan yang terhubung adalah Taranium Testnet
      const { chainId } = await provider.getNetwork();
      if (chainId !== TARANIUM_CHAIN_ID) {
        alert(`Harap beralih ke Taranium Smartchain Testnet (Chain ID: ${TARANIUM_CHAIN_ID}).`);
        // Anda bisa tambahkan logika untuk meminta pengguna beralih jaringan di MetaMask
        // await window.ethereum.request({
        //   method: 'wallet_addEthereumChain',
        //   params: [{
        //     chainId: '0x' + TARANIUM_CHAIN_ID.toString(16), // Chain ID dalam heksadesimal
        //     rpcUrls: [TARANIUM_RPC_URL],
        //     chainName: 'Taranium Smartchain Testnet',
        //     nativeCurrency: { name: 'TARAN', symbol: 'TARAN', decimals: 18 }, // Ganti dengan detail token Taranium
        //     blockExplorerUrls: ['https://testnet-scan.taranium.com/']
        //   }]
        // });
        return null;
      }

      return signer;
    } catch (error) {
      console.error("Kesalahan saat menghubungkan ke dompet:", error);
      alert("Gagal terhubung ke dompet. Pastikan MetaMask atau dompet lain terinstal.");
      return null;
    }
  } else {
    alert("Dompet web3 (misalnya MetaMask) tidak terinstal. Silakan instal untuk melanjutkan.");
    return null;
  }
}

/**
 * Mendaftarkan hash dokumen ke smart contract di Taranium Smartchain.
 * Hanya untuk institusi yang login (melalui frontend).
 * @param {string} documentHash Hash SHA-256 dari dokumen.
 * @param {string} ownerAddress Alamat dompet pemilik dokumen (bisa alamat institusi itu sendiri atau mahasiswa).
 * @param {string} metadataURI URI metadata jika ada (misal, link ke IPFS untuk detail).
 * @returns {Promise<string|null>} Transaction hash jika berhasil, null jika gagal.
 */
export async function registerHashOnChain(documentHash, ownerAddress, metadataURI = "") {
  const signer = await connectWallet();
  if (!signer) return null;

  try {
    const contract = new ethers.Contract(CERTIFICATE_REGISTRY_ADDRESS, CERTIFICATE_REGISTRY_ABI, signer);
    // documentHash harus dalam format bytes32, jadi pastikan sudah 0x-prefixed dan panjang 32 byte.
    // Jika hash dari backend adalah string heksadesimal, pastikan itu sudah 64 karakter.
    const tx = await contract.registerDocument(documentHash, ownerAddress, metadataURI);
    await tx.wait(); // Tunggu hingga transaksi dikonfirmasi
    console.log("Transaksi pendaftaran hash berhasil:", tx.hash);
    return tx.hash;
  } catch (error) {
    console.error("Gagal mendaftarkan hash ke blockchain:", error);
    alert("Gagal mendaftarkan hash ke blockchain. Lihat konsol untuk detail.");
    return null;
  }
}

/**
 * Mengecek keberadaan hash dokumen di smart contract Taranium Smartchain.
 * Untuk user umum (tidak perlu login ke backend, hanya perlu wallet terhubung jika perlu).
 * @param {string} documentHash Hash SHA-256 dari dokumen yang akan dicek.
 * @returns {Promise<boolean>} True jika hash terdaftar, false jika tidak.
 */
export async function checkHashOnChain(documentHash) {
  // Untuk fungsi view, kita bisa menggunakan provider tanpa signer (tidak perlu sign transaksi)
  let provider;
  if (window.ethereum) {
    provider = new ethers.BrowserProvider(window.ethereum);
  } else {
    // Fallback ke JsonRpcProvider jika MetaMask tidak terinstal, hanya untuk fungsi view
    provider = new ethers.JsonRpcProvider(TARANIUM_RPC_URL);
  }

  try {
    const contract = new ethers.Contract(CERTIFICATE_REGISTRY_ADDRESS, CERTIFICATE_REGISTRY_ABI, provider);
    // Panggil fungsi view di smart contract
    const isRegistered = await contract.isDocumentRegistered(documentHash);
    console.log(`Hash ${documentHash} terdaftar: ${isRegistered}`);
    return isRegistered;
  } catch (error) {
    console.error("Gagal mengecek hash di blockchain:", error);
    // Jika terjadi error, asumsikan tidak terdaftar untuk keamanan
    return false;
  }
}