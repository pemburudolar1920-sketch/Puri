package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Pengumuman
import kotlinx.coroutines.flow.Flow

@Dao
interface PengumumanDao {
    @Query("SELECT * FROM pengumuman ORDER BY isPrioritas DESC, id DESC")
    fun getAllPengumuman(): Flow<List<Pengumuman>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengumuman(pengumuman: Pengumuman): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Pengumuman>)

    @Update
    suspend fun updatePengumuman(pengumuman: Pengumuman)

    @Query("UPDATE pengumuman SET rsvpWargaList = :rsvpList WHERE id = :id")
    suspend fun updateRsvpList(id: Long, rsvpList: String)

    @Delete
    suspend fun deletePengumuman(pengumuman: Pengumuman)

    @Query("SELECT COUNT(*) FROM pengumuman")
    suspend fun getCount(): Int
}
