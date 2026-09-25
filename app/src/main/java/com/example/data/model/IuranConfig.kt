package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "iuran_config")
data class IuranConfig(
    @PrimaryKey val id: Int = 1,
    val iuranKeamanan: Long = 60000L,
    val iuranKebersihan: Long = 40000L,
    val iuranKasRt: Long = 30000L,
    val iuranSosial: Long = 20000L,
    val namaRekening: String = "Kas RT PURI PRATAMA",
    val bankRekening: String = "Bank Mandiri",
    val noRekening: String = "132-00-987654-2",
    val namaBendahara: String = "Bpk. Bambang Sutrisno (Bendahara RT)",
    val kontakBendaharaWa: String = "6281234567890",
    val keteranganTambahan: String = "Pembayaran paling lambat tanggal 10 setiap bulan"
) {
    val totalIuran: Long
        get() = iuranKeamanan + iuranKebersihan + iuranKasRt + iuranSosial
}
