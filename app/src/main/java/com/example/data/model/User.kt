package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    WARGA,
    PENGURUS_RT
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, // NIK or Phone or Unique Username
    val passwordHash: String,
    val namaLengkap: String,
    val noHp: String,
    val noNik: String,
    val blokRumah: String,
    val role: UserRole = UserRole.WARGA
)
