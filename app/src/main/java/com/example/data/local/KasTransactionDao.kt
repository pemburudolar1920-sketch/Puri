package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.KasTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface KasTransactionDao {
    @Query("SELECT * FROM kas_transactions ORDER BY tanggal DESC, id DESC")
    fun getAllTransactions(): Flow<List<KasTransaction>>

    @Query("SELECT * FROM kas_transactions WHERE bulanTahun = :bulanTahun ORDER BY tanggal DESC, id DESC")
    fun getTransactionsByMonth(bulanTahun: String): Flow<List<KasTransaction>>

    @Query("SELECT * FROM kas_transactions WHERE jenis = 'PENGELUARAN' ORDER BY tanggal DESC, id DESC")
    fun getAllPengeluaran(): Flow<List<KasTransaction>>

    @Query("SELECT * FROM kas_transactions WHERE jenis = 'PEMASUKAN' ORDER BY tanggal DESC, id DESC")
    fun getAllPemasukan(): Flow<List<KasTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KasTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<KasTransaction>)

    @Delete
    suspend fun deleteTransaction(transaction: KasTransaction)

    @Query("SELECT COUNT(*) FROM kas_transactions")
    suspend fun getCount(): Int
}
