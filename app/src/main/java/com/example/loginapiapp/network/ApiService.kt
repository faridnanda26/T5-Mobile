package com.example.loginapiapp.network

import com.example.loginapiapp.model.ApiResponse
import com.example.loginapiapp.model.LoginRequest
import com.example.loginapiapp.model.LoginResponse
import com.example.loginapiapp.model.Pasien
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("pasien")
    suspend fun getPasien(
        @Header("Authorization") token: String // 🌐 TAMBAHKAN BARIS INI
    ): Response<ApiResponse<List<Pasien>>>

    // 2. CREATE PASIEN (Tambah Pasien Baru)
    @POST("pasien")
    suspend fun createPasien(
        @Header("Authorization") token: String,
        @Body pasien: Pasien
    ): Response<ApiResponse<Pasien>>

    // 3. UPDATE PASIEN (Ubah Data Pasien Berdasarkan ID)
    @PUT("pasien/{id}")
    suspend fun updatePasien(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body pasien: Pasien
    ): Response<ApiResponse<Pasien>>

    // 4. DELETE PASIEN (Hapus Pasien Berdasarkan ID)
    @DELETE("pasien/{id}")
    suspend fun deletePasien(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Unit>> // Menggunakan ApiResponse<Unit> atau Response<Unit> sesuai standard API backend-mu
}