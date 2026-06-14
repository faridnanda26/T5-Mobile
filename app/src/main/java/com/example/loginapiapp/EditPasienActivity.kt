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

class EditPasienActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etTanggalLahir: TextInputEditText
    private lateinit var etGender: TextInputEditText
    private lateinit var etTelepon: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var btnUpdate: MaterialButton
    private lateinit var btnBatal: MaterialButton

    // Variabel penampung ID pasien yang akan diubah
    private var pasienId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pasien)

        // 1. Hubungkan variabel dengan ID XML
        etNama = findViewById(R.id.etNama)
        etTanggalLahir = findViewById(R.id.etTanggalLahir)
        etGender = findViewById(R.id.etGender)
        etTelepon = findViewById(R.id.etTelepon)
        etAlamat = findViewById(R.id.etAlamat)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnBatal = findViewById(R.id.btnBatal)

        // 2. Ambil data lama yang dilempar dari HomeActivity / PasienHomeActivity
        pasienId = intent.getIntExtra("PASIEN_ID", -1)
        val namaLama = intent.getStringExtra("PASIEN_NAMA") ?: ""
        val tglLahirLama = intent.getStringExtra("PASIEN_TGL_LAHIR") ?: ""
        val genderLama = intent.getStringExtra("PASIEN_GENDER") ?: ""
        val alamatLama = intent.getStringExtra("PASIEN_ALAMAT") ?: ""
        val teleponLama = intent.getStringExtra("PASIEN_TELEPON") ?: ""

        // 3. Set data lama tersebut ke dalam form input biar user tinggal edit bagian tertentu
        etNama.setText(namaLama)
        etTanggalLahir.setText(tglLahirLama)
        etGender.setText(genderLama)
        etTelepon.setText(teleponLama)
        etAlamat.setText(alamatLama)

        // 4. Tombol Batal
        btnBatal.setOnClickListener { finish() }

        // 5. Tombol Update Eksekusi
        btnUpdate.setOnClickListener {
            perbaruiPasienKeServer()
        }
    }

    private fun perbaruiPasienKeServer() {
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

        if (pasienId == -1) {
            Toast.makeText(this, "ID Pasien tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        // BUNGKUS KE MODEL DATA BARU (Gunakan ID lama agar menimpa record yang tepat di database server)
        val pasienUpdate = Pasien(
            id = pasienId,
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

        // PROSES PUT DATA LEWAT RETROFIT
        lifecycleScope.launch {
            try {
                btnUpdate.isEnabled = false // Kunci tombol saat memproses kiriman data

                // ✅ SEKARANG BENAR: Menyertakan token sesuai spesifikasi ApiService kamu yang baru (3 parameter)
                val response = RetrofitClient.apiService.updatePasien(token, pasienId, pasienUpdate)

                if (response.isSuccessful) {
                    Toast.makeText(this@EditPasienActivity, "Data $nama berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish() // Tutup halaman, otomatis memicu load data terbaru di halaman utama
                } else {
                    Toast.makeText(this@EditPasienActivity, "Gagal memperbarui: ${response.code()}", Toast.LENGTH_SHORT).show()
                    btnUpdate.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditPasienActivity, "Error Koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
                btnUpdate.isEnabled = true
            }
        }
    }
}