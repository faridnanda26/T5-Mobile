package com.example.loginapiapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapiapp.model.PasienAdapter
import com.example.loginapiapp.model.Pasien
import com.example.loginapiapp.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var rvPasien: RecyclerView
    private lateinit var tvNama: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: PasienAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Inisialisasi view
        rvPasien = findViewById(R.id.rvPasien)
        progressBar = findViewById(R.id.progressBar)
        fabAdd = findViewById(R.id.fabAdd)
        tvNama = findViewById(R.id.tvNamaPasien)

        // 2. Setup RecyclerView dengan lambda untuk Edit dan Delete
        adapter = PasienAdapter(
            onEditClick = { pasien ->
                // Buka EditPasienActivity sambil membawa data pasien saat ini
                val intent = Intent(this, EditPasienActivity::class.java).apply {
                    putExtra("PASIEN_ID", pasien.id)
                    putExtra("PASIEN_NAMA", pasien.nama)
                    putExtra("PASIEN_TGL_LAHIR", pasien.tanggal_lahir)
                    putExtra("PASIEN_GENDER", pasien.jenis_kelamin)
                    putExtra("PASIEN_ALAMAT", pasien.alamat)
                    putExtra("PASIEN_TELEPON", pasien.no_telepon)
                }
                startActivity(intent)
            },
            onDeleteClick = { pasien ->
                // Tampilkan dialog konfirmasi hapus
                showDeleteConfirmation(pasien)
            }
        )
        rvPasien.layoutManager = LinearLayoutManager(this)
        rvPasien.adapter = adapter

        // 3. Setup FAB - Buka AddPasienActivity
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddPasienActivity::class.java)
            startActivity(intent)
        }

        // ⚠️ 5. Tangkap data nama dari MainActivity dan tampilkan ke TextView
        val namaUser = intent.getStringExtra("extra_name") ?: "User"
        tvNama.text = "Selamat Datang, $namaUser"

        // 4. Load data awal dari API
        loadPasien()
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
    }

    // Dipanggil saat Activity kembali ke foreground
    override fun onResume() {
        super.onResume()
        // Refresh data pasien setiap kali kembali ke halaman ini
        loadPasien()
    }

    // MENAMPILKAN DIALOG KONFIRMASI SEBELUM MENGHAPUS DATA
    private fun showDeleteConfirmation(pasien: Pasien) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus pasien ${pasien.nama}?")
            .setPositiveButton("Hapus") { dialog, _ ->
                deletePasien(pasien)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // OPERASI MENGHAPUS DATA PASIEN MELALUI API (DENGAN TOKEN)
    private fun deletePasien(pasien: Pasien) {
        val id = pasien.id ?: run {
            showMessage("ID pasien tidak valid")
            return
        }

        // 🔑 Ambil token untuk Authorization Header
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"

        lifecycleScope.launch {
            showLoading(true)

            try {
                // ✅ Menyertakan parameter token ke ApiService
                val response = RetrofitClient.apiService.deletePasien(token, id)

                if (response.isSuccessful) {
                    showMessage("Data pasien berhasil dihapus")
                    loadPasien() // Refresh list
                } else {
                    showMessage("Gagal menghapus data: ${response.code()}")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    // OPERASI MENGAMBIL SELURUH DATA PASIEN DARI API SERVER (DENGAN TOKEN)
    private fun loadPasien() {
        // 🔑 Ambil token untuk Authorization Header
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"

        lifecycleScope.launch {
            showLoading(true)

            try {
                // ✅ Menyertakan parameter token ke ApiService
                val response = RetrofitClient.apiService.getPasien(token)

                if (response.isSuccessful) {
                    val daftarPasien = response.body()?.data ?: emptyList()
                    adapter.setData(daftarPasien)

                    if (daftarPasien.isEmpty()) {
                        showMessage("Belum ada data pasien")
                    }
                } else {
                    showMessage("Gagal mengambil data: ${response.code()}")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}