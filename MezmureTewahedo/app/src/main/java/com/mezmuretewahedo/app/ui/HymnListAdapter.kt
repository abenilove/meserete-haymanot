package com.mezmuretewahedo.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mezmuretewahedo.app.data.Hymn
import com.mezmuretewahedo.app.databinding.ItemCategoryHeaderBinding
import com.mezmuretewahedo.app.databinding.ItemHymnBinding

private const val TYPE_HEADER = 0
private const val TYPE_ROW = 1

class HymnListAdapter(
    private val onHeaderClick: (String) -> Unit,
    private val onHymnClick: (Hymn) -> Unit,
    private val onFavoriteClick: (Hymn) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Row -> TYPE_ROW
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(ItemCategoryHeaderBinding.inflate(inflater, parent, false))
        } else {
            RowVH(ItemHymnBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.Header -> (holder as HeaderVH).bind(item)
            is ListItem.Row -> (holder as RowVH).bind(item.hymn)
        }
    }

    inner class HeaderVH(private val binding: ItemCategoryHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: ListItem.Header) {
            binding.textCategory.text = header.category
            binding.textCount.text = header.count.toString()
            binding.imageChevron.rotation = if (header.expanded) 180f else 0f
            binding.root.setOnClickListener { onHeaderClick(header.category) }
        }
    }

    inner class RowVH(private val binding: ItemHymnBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(hymn: Hymn) {
            binding.textNum.text = if (hymn.num > 0) "${hymn.num}." else "•"
            binding.textTitle.text = hymn.title
            binding.imageFavorite.setImageResource(
                if (hymn.isFavorite) com.mezmuretewahedo.app.R.drawable.ic_favorite_filled
                else com.mezmuretewahedo.app.R.drawable.ic_favorite_border
            )
            binding.root.setOnClickListener { onHymnClick(hymn) }
            binding.imageFavorite.setOnClickListener { onFavoriteClick(hymn) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ListItem>() {
            override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
                when {
                    oldItem is ListItem.Header && newItem is ListItem.Header -> oldItem.category == newItem.category
                    oldItem is ListItem.Row && newItem is ListItem.Row -> oldItem.hymn.id == newItem.hymn.id
                    else -> false
                }

            override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
                oldItem == newItem
        }
    }
}
