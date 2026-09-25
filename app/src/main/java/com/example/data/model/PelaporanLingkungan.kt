package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pelaporan_lingkungan")
data class PelaporanLingkungan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val judul: String,
    val deskripsi: String,
    val kategori: String, // Lampu Jalan, Sampah, Saluran Air, Keamanan, Fasum, Jalan, Lainnya
    val lokasiDetail: String,
    val fotoUri: String = "",
    val namaPelapor: String,
    val kontakPelapor: String,
    val status: String = "TERKIRIM", // TERKIRIM, DIPROSES, SELESAI
    val tanggalLapor: String,
    val tanggapanPengurus: String = "",
    val tanggalSelesai: String = ""
)
