package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warga")
data class Warga(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String,
    val nik: String,
    val noKk: String,
    val noHp: String,
    val blokRumah: String,
    val statusHunian: String = "Pemilik Tetap", // "Pemilik Tetap" atau "Kontrak / Sewa"
    val jumlahKeluarga: Int = 3,
    val statusIuranBulanIni: Boolean = false, // true = Lunas, false = Belum Bayar
    val bulanTagihan: String = "September 2026",
    val tanggalBayar: String = "",
    val totalIuranNominal: Long = 150000L
)
