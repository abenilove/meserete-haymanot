package com.mezmuretewahedo.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class HymnRepository(private val dao: HymnDao) {

    fun observeAll(): Flow<List<Hymn>> = dao.observeAll()

    fun observeFavorites(): Flow<List<Hymn>> = dao.observeFavorites()

    fun search(query: String): Flow<List<Hymn>> = dao.search(query)

    suspend fun add(hymn: Hymn): Long = dao.insert(hymn)

    suspend fun update(hymn: Hymn) = dao.update(hymn)

    suspend fun delete(hymn: Hymn) = dao.delete(hymn)

    suspend fun getById(id: Long): Hymn? = dao.getById(id)

    suspend fun allCategories(): List<String> = dao.allCategories()

    /** Populates the database with the bundled hymnal the very first time the app runs. */
    suspend fun ensureSeeded(context: Context) {
        if (dao.count() == 0) {
            dao.insertAll(SeedLoader.loadBundledHymns(context))
        }
    }

    /** Scans the on-device "ImportLyrics" folder for new .txt files and adds them as hymns. */
    suspend fun importFromFolder(context: Context): Int {
        val existingTitles = dao.allTitles().map { it.trim().lowercase() }.toHashSet()
        val newHymns = LyricsFileImporter.scanAndParse(context, existingTitles)
        if (newHymns.isNotEmpty()) {
            dao.insertAll(newHymns)
        }
        return newHymns.size
    }
}
