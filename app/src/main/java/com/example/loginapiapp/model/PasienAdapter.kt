package com.example.loginapiapp.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapiapp.R
import com.example.loginapiapp.model.Pasien // Sesuaikan dengan nama data class pasien kamu

class PasienAdapter(
    private val onEditClick: (Pasien) -> Unit,   // Lambda untuk handle aksi klik Edit
    private val onDeleteClick: (Pasien) -> Unit   // Lambda untuk handle aksi klik Hapus
) : RecyclerView.Adapter<PasienAdapter.PasienViewHolder>() {

    // List lokal untuk menampung data pasien dari API/Database
    private val daftarPasien = mutableListOf<Pasien>()

    // Fungsi untuk memperbarui data list di dalam adapter
    fun setData(newPasienList: List<Pasien>) {
        daftarPasien.clear()
        daftarPasien.addAll(newPasienList)
        notifyDataSetChanged() // Memberitahu RecyclerView untuk menggambar ulang antarmuka
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasienViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pasien, parent, false)
        return PasienViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasienViewHolder, position: Int) {
        holder.bind(daftarPasien[position])
    }

    override fun getItemCount(): Int = daftarPasien.size

    // ViewHolder untuk memetakan dan mengikat data ke komponen XML item_pasien
    inner class PasienViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPasien: TextView = itemView.findViewById(R.id.tvNamaPasien)
        private val tvDetailPasien: TextView = itemView.findViewById(R.id.tvDetailPasien)
        private val tvAlamatPasien: TextView = itemView.findViewById(R.id.tvAlamatPasien)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(pasien: Pasien) {
            // 1. Set nama pasien
            tvNamaPasien.text = pasien.nama

            // 2. Format jenis kelamin (L -> Laki-laki, P -> Perempuan) + Nomor Telepon
            val genderLengkap = if (pasien.jenis_kelamin == "L") "Laki-laki" else "Perempuan"
            tvDetailPasien.text = "$genderLengkap - ${pasien.no_telepon}"

            // 3. Set alamat pasien
            tvAlamatPasien.text = pasien.alamat

            // 4. Handle Trigger Klik Tombol Edit
            btnEdit.setOnClickListener {
                onEditClick(pasien)
            }

            // 5. Handle Trigger Klik Tombol Hapus
            btnDelete.setOnClickListener {
                onDeleteClick(pasien)
            }
        }
    }
}