package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PelaporanLingkungan
import kotlinx.coroutines.flow.Flow

@Dao
interface PelaporanDao {
    @Query("SELECT * FROM pelaporan_lingkungan ORDER BY id DESC")
    fun getAllLaporan(): Flow<List<PelaporanLingkungan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaporan(laporan: PelaporanLingkungan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<PelaporanLingkungan>)

    @Update
    suspend fun updateLaporan(laporan: PelaporanLingkungan)

    @Query("UPDATE pelaporan_lingkungan SET status = :status, tanggapanPengurus = :tanggapan, tanggalSelesai = :tanggalSelesai WHERE id = :id")
    suspend fun updateStatusAndResponse(id: Long, status: String, tanggapan: String, tanggalSelesai: String)

    @Delete
    suspend fun deleteLaporan(laporan: PelaporanLingkungan)

    @Query("SELECT COUNT(*) FROM pelaporan_lingkungan")
    suspend fun getCount(): Int
}
