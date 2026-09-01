package com.mezmuretewahedo.app.data

import android.content.Context
import java.io.File

/**
 * Watches a plain folder on the device's external app storage:
 *   Android/data/com.mezmuretewahedo.app/files/ImportLyrics/
 *
 * Anyone can drop a .txt file in there (via a file manager, USB, cloud sync app, etc.)
 * and the app will pick it up as a new hymn the next time "Sync" is pressed or the app
 * is opened. This needs no runtime permission on modern Android since it's the app's
 * own external files directory.
 *
 * Expected file format (simple and forgiving):
 *
 *   Title: <hymn title>          (or just the title as the very first line)
 *   Category: <category name>    (optional)
 *   ---                          (optional separator line)
 *   <lyrics, any number of lines>
 */
object LyricsFileImporter {

    const val IMPORT_FOLDER_NAME = "ImportLyrics"
    private const val IMPORTED_SUBFOLDER = "Imported"
    private const val DEFAULT_CATEGORY = "የተጨመሩ መዝሙራት" // "Added Hymns"

    fun importFolder(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), IMPORT_FOLDER_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun scanAndParse(context: Context, existingTitlesLowercase: Set<String>): List<Hymn> {
        val dir = importFolder(context)
        val importedDir = File(dir, IMPORTED_SUBFOLDER).apply { mkdirs() }

        val txtFiles = dir.listFiles { f -> f.isFile && f.extension.equals("txt", ignoreCase = true) }
            ?: return emptyList()

        val results = ArrayList<Hymn>()
        for (file in txtFiles) {
            val parsed = runCatching { parseFile(file) }.getOrNull() ?: continue
            if (parsed.title.isBlank() || parsed.lyrics.isBlank()) continue
            if (parsed.title.trim().lowercase() in existingTitlesLowercase) {
                // Already have a hymn with this title -- still archive the file so it's not rescanned forever.
                moveToImported(file, importedDir)
                continue
            }
            results.add(
                Hymn(
                    title = parsed.title.trim(),
                    category = parsed.category.ifBlank { DEFAULT_CATEGORY },
                    lyrics = parsed.lyrics.trim(),
                    isUserAdded = true,
                    sourceFile = file.name
                )
            )
            moveToImported(file, importedDir)
        }
        return results
    }

    private fun moveToImported(file: File, importedDir: File) {
        val target = File(importedDir, file.name)
        runCatching {
            if (target.exists()) target.delete()
            file.copyTo(target, overwrite = true)
            file.delete()
        }
    }

    private data class ParsedFile(val title: String, val category: String, val lyrics: String)

    private fun parseFile(file: File): ParsedFile {
        val lines = file.readText(Charsets.UTF_8).lines().toMutableList()
        // drop leading blank lines
        while (lines.isNotEmpty() && lines.first().isBlank()) lines.removeAt(0)
        if (lines.isEmpty()) return ParsedFile("", "", "")

        var title = ""
        var category = ""
        var idx = 0

        val firstLine = lines[0]
        title = when {
            firstLine.startsWith("Title:", ignoreCase = true) -> firstLine.substringAfter(":").trim()
            firstLine.startsWith("ርዕስ:", ignoreCase = true) -> firstLine.substringAfter(":").trim()
            else -> firstLine.trim()
        }
        idx = 1

        if (idx < lines.size) {
            val next = lines[idx]
            if (next.startsWith("Category:", ignoreCase = true) || next.startsWith("ምድብ:", ignoreCase = true)) {
                category = next.substringAfter(":").trim()
                idx++
            }
        }

        if (idx < lines.size && lines[idx].trim() == "---") {
            idx++
        }

        val lyrics = lines.drop(idx).joinToString("\n").trim()

        if (title.isBlank()) {
            title = file.nameWithoutExtension.replace('_', ' ').trim()
        }

        return ParsedFile(title, category, lyrics)
    }
}
