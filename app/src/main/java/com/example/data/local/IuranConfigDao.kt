package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.IuranConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface IuranConfigDao {
    @Query("SELECT * FROM iuran_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<IuranConfig?>

    @Query("SELECT * FROM iuran_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigOnce(): IuranConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: IuranConfig)

    @Update
    suspend fun update(config: IuranConfig)
}
