package com.mezmuretewahedo.app.data

import android.content.Context
import org.json.JSONArray

/**
 * Reads the bundled assets/hymns.json (the original Mezmure Tewahedo hymnal,
 * converted from the legacy-font source document) and turns it into Hymn rows.
 * This only runs once, the first time the app launches on a device.
 */
object SeedLoader {

    fun loadBundledHymns(context: Context): List<Hymn> {
        val jsonText = context.assets.open("hymns.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val array = JSONArray(jsonText)
        val list = ArrayList<Hymn>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Hymn(
                    num = obj.optInt("num", 0),
                    title = obj.optString("title").trim(),
                    category = obj.optString("category", "Miscellaneous").trim(),
                    lyrics = obj.optString("lyrics_text").trim(),
                    isFavorite = false,
                    isUserAdded = false,
                    sourceFile = "hymns.json"
                )
            )
        }
        return list
    }
}
