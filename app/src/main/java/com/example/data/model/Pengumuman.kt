package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pengumuman")
data class Pengumuman(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val judul: String,
    val isi: String,
    val tanggalKegiatan: String,
    val jamKegiatan: String,
    val lokasi: String,
    val kategori: String, // Kerja Bakti, Rapat Warga, Posyandu, Ronda Malam, Acara RT, Informasi
    val dibuatOleh: String = "Pengurus RT Puri Pratama",
    val tanggalPost: String,
    val rsvpWargaList: String = "", // Comma-separated citizen names who confirmed attendance
    val isPrioritas: Boolean = false
) {
    val rsvpCount: Int
        get() = if (rsvpWargaList.isBlank()) 0 else rsvpWargaList.split(",").filter { it.isNotBlank() }.size

    fun isUserAttending(userName: String): Boolean {
        if (rsvpWargaList.isBlank()) return false
        return rsvpWargaList.split(",").map { it.trim().lowercase() }.contains(userName.trim().lowercase())
    }
}
