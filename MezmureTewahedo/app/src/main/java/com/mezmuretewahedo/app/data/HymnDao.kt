package com.mezmuretewahedo.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HymnDao {

    @Query("SELECT * FROM hymns ORDER BY category ASC, num ASC, title ASC")
    fun observeAll(): Flow<List<Hymn>>

    @Query("SELECT * FROM hymns WHERE isFavorite = 1 ORDER BY category ASC, title ASC")
    fun observeFavorites(): Flow<List<Hymn>>

    @Query("""
        SELECT * FROM hymns
        WHERE title LIKE '%' || :query || '%' OR lyrics LIKE '%' || :query || '%'
        ORDER BY category ASC, title ASC
    """)
    fun search(query: String): Flow<List<Hymn>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(hymns: List<Hymn>)

    @Insert
    suspend fun insert(hymn: Hymn): Long

    @Update
    suspend fun update(hymn: Hymn)

    @Delete
    suspend fun delete(hymn: Hymn)

    @Query("SELECT COUNT(*) FROM hymns")
    suspend fun count(): Int

    @Query("SELECT title FROM hymns")
    suspend fun allTitles(): List<String>

    @Query("SELECT DISTINCT category FROM hymns ORDER BY category ASC")
    suspend fun allCategories(): List<String>

    @Query("SELECT * FROM hymns WHERE id = :id")
    suspend fun getById(id: Long): Hymn?
}
