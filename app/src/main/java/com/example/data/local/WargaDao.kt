package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Warga
import kotlinx.coroutines.flow.Flow

@Dao
interface WargaDao {
    @Query("SELECT * FROM warga ORDER BY blokRumah ASC")
    fun getAllWarga(): Flow<List<Warga>>

    @Query("SELECT * FROM warga WHERE statusIuranBulanIni = 0 ORDER BY blokRumah ASC")
    fun getWargaBelumBayar(): Flow<List<Warga>>

    @Query("SELECT * FROM warga WHERE statusIuranBulanIni = 1 ORDER BY blokRumah ASC")
    fun getWargaLunas(): Flow<List<Warga>>

    @Query("SELECT * FROM warga WHERE id = :id LIMIT 1")
    suspend fun getWargaById(id: Long): Warga?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarga(warga: Warga): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wargaList: List<Warga>)

    @Update
    suspend fun updateWarga(warga: Warga)

    @Delete
    suspend fun deleteWarga(warga: Warga)

    @Query("UPDATE warga SET statusIuranBulanIni = :status, tanggalBayar = :tanggal WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, status: Boolean, tanggal: String)

    @Query("SELECT COUNT(*) FROM warga")
    suspend fun getCount(): Int
}
