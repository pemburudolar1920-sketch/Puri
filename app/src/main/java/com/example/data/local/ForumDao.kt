package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ForumPost
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Query("SELECT * FROM forum_posts ORDER BY id DESC")
    fun getAllPosts(): Flow<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ForumPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ForumPost>)

    @Update
    suspend fun updatePost(post: ForumPost)

    @Query("UPDATE forum_posts SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun upvote(id: Long)

    @Query("UPDATE forum_posts SET komentarRaw = :komentarRaw WHERE id = :id")
    suspend fun updateComments(id: Long, komentarRaw: String)

    @Delete
    suspend fun deletePost(post: ForumPost)

    @Query("SELECT COUNT(*) FROM forum_posts")
    suspend fun getCount(): Int
}
