package com.example.loginapiapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginapiapp.model.LoginRequest
import com.example.loginapiapp.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hubungkan variabel dengan view di layout
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        // Panggil fungsi login saat tombol diklik
        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        // Ambil teks dari input dan hapus spasi di awal/akhir
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validasi: pastikan kedua field tidak kosong
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Email dan password wajib diisi")
            return
        }

        // Jalankan request di coroutine agar tidak memblokir UI
        lifecycleScope.launch {
            showLoading(true)

            try {
                // Buat objek request dan kirim ke API
                val request = LoginRequest(email, password)
                val response = RetrofitClient.apiService.login(request)

                if (response.isSuccessful) {
                    val loginData = response.body()?.data
                    val userName = loginData?.user?.name.orEmpty()
                    val token = loginData?.token.orEmpty()      // <-- TAMBAHKAN baris ini

                    if (userName.isNotEmpty()) {
                        // TAMBAHKAN dua baris ini untuk menyimpan token
                        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                        prefs.edit().putString("token", token).apply()

                        // Baris di bawah ini sudah ada sebelumnya, tidak perlu diubah
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        intent.putExtra(HomeActivity.EXTRA_NAME, userName)
                        startActivity(intent)
                    } else {
                        showMessage("Data user tidak ditemukan")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Email atau password salah"
                    showMessage(errorMessage)
                }
            } catch (e: Exception) {
                showMessage("Tidak dapat terhubung ke server: ${e.message}")
            } finally {
                // Sembunyikan loading baik berhasil maupun gagal
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}