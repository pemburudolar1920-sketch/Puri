package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kas_transactions")
data class KasTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jenis: String, // "PEMASUKAN" atau "PENGELUARAN"
    val kategori: String,
    val keterangan: String,
    val nominal: Long,
    val tanggal: String, // format YYYY-MM-DD
    val bulanTahun: String = "September 2026",
    val dicatatOleh: String = "Pengurus RT",
    val buktiRef: String = ""
)
