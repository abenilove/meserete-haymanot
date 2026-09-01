package com.mezmuretewahedo.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hymns")
data class Hymn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val num: Int = 0,
    val title: String,
    val category: String,
    val lyrics: String,
    val isFavorite: Boolean = false,
    val isUserAdded: Boolean = false,
    val sourceFile: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)
