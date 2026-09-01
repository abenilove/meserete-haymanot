package com.mezmuretewahedo.app.ui

import com.mezmuretewahedo.app.data.Hymn

sealed class ListItem {
    data class Header(val category: String, val count: Int, val expanded: Boolean) : ListItem()
    data class Row(val hymn: Hymn) : ListItem()
}

/** Groups a flat hymn list into collapsible category sections, in-order. */
fun buildListItems(hymns: List<Hymn>, expandedCategories: Set<String>, forceFlat: Boolean = false): List<ListItem> {
    if (forceFlat) {
        return hymns.map { ListItem.Row(it) }
    }
    val items = ArrayList<ListItem>()
    val grouped = hymns.groupBy { it.category }
    for ((category, hymnsInCategory) in grouped) {
        val expanded = category in expandedCategories
        items.add(ListItem.Header(category, hymnsInCategory.size, expanded))
        if (expanded) {
            hymnsInCategory.forEach { items.add(ListItem.Row(it)) }
        }
    }
    return items
}
