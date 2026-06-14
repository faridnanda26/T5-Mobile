package com.example.loginapiapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginapiapp.model.Pasien
import com.example.loginapiapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddPasienActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etTanggalLahir: TextInputEditText
    private lateinit var etGender: TextInputEditText
    private lateinit var etTelepon: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBatal: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pasien)

        // 1. Hubungkan variabel dengan ID XML
        etNama = findViewById(R.id.etNama)
        etTanggalLahir = findViewById(R.id.etTanggalLahir)
        etGender = findViewById(R.id.etGender)
        etTelepon = findViewById(R.id.etTelepon)
        etAlamat = findViewById(R.id.etAlamat)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBatal = findViewById(R.id.btnBatal)

        // 2. Tombol Batal Keluar
        btnBatal.setOnClickListener { finish() }

        // 3. Tombol Simpan Aksi
        btnSimpan.setOnClickListener {
            simpanPasienKeServer()
        }
    }

    private fun simpanPasienKeServer() {
        val nama = etNama.text.toString().trim()
        val tglLahir = etTanggalLahir.text.toString().trim()
        val gender = etGender.text.toString().trim().uppercase()
        val telepon = etTelepon.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()

        // VALIDASI FORM KOSONG
        if (nama.isEmpty() || tglLahir.isEmpty() || gender.isEmpty() || telepon.isEmpty() || alamat.isEmpty()) {
            Toast.makeText(this, "Semua data pasien wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        if (gender != "L" && gender != "P") {
            etGender.error = "Gunakan huruf L atau P"
            etGender.requestFocus()
            return
        }

        // BUNGKUS KE MODEL DATA (id diisi 0 karena otomatis auto-increment oleh backend)
        val pasienBaru = Pasien(
            id = 0,
            nama = nama,
            tanggal_lahir = tglLahir,
            jenis_kelamin = gender,
            alamat = alamat,
            no_telepon = telepon,
            created_at = "",
            updated_at = ""
        )

        // 🔑 Ambil token autentikasi dari SharedPreferences lokal
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"

        // PROSES KIRIM DATA LEWAT RETROFIT (COROUTINES)
        lifecycleScope.launch {
            try {
                btnSimpan.isEnabled = false // Kunci tombol agar tidak dobel klik saat loading

                // ✅ SEKARANG BENAR: Menyertakan token sesuai spesifikasi ApiService (2 parameter: token & objek)
                val response = RetrofitClient.apiService.createPasien(token, pasienBaru)

                if (response.isSuccessful) {
                    Toast.makeText(this@AddPasienActivity, "Pasien $nama sukses ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish() // Tutup halaman, otomatis trigger onResume di Home untuk auto-refresh list
                } else {
                    Toast.makeText(this@AddPasienActivity, "Gagal menyimpan: ${response.code()}", Toast.LENGTH_SHORT).show()
                    btnSimpan.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddPasienActivity, "Error Koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
            }
        }
    }
}