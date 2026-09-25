package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ForumKomentar(
    val id: String,
    val nama: String,
    val role: String,
    val blok: String,
    val komentar: String,
    val waktu: String
)

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val judul: String,
    val isi: String,
    val kategori: String, // "Saran & Masukan", "Fasilitas Umum", "Keamanan", "Usulan Kegiatan", "Musyawarah"
    val penulisNama: String,
    val penulisRole: String, // "Warga Blok B", "Ketua RT", "Pengurus"
    val blokRumah: String,
    val tanggal: String,
    val upvotes: Int = 0,
    val komentarRaw: String = "" // Pipe-delimited or json format: "Nama::Role::Waktu::Isi||Nama2::Role::..."
) {
    fun parseComments(): List<ForumKomentar> {
        if (komentarRaw.isBlank()) return emptyList()
        return komentarRaw.split("||").mapNotNull { item ->
            val parts = item.split("::")
            if (parts.size >= 5) {
                ForumKomentar(
                    id = parts[0],
                    nama = parts[1],
                    role = parts[2],
                    waktu = parts[3],
                    blok = "",
                    komentar = parts[4]
                )
            } else null
        }
    }
}
