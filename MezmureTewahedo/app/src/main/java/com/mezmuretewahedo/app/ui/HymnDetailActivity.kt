package com.mezmuretewahedo.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mezmuretewahedo.app.MezmurApp
import com.mezmuretewahedo.app.R
import com.mezmuretewahedo.app.data.Hymn
import com.mezmuretewahedo.app.data.HymnRepository
import com.mezmuretewahedo.app.databinding.ActivityHymnDetailBinding
import kotlinx.coroutines.launch

class HymnDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHymnDetailBinding
    private lateinit var repository: HymnRepository
    private var hymn: Hymn? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHymnDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as MezmurApp).repository

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.buttonFavorite.setOnClickListener { toggleFavorite() }
        binding.buttonShare.setOnClickListener { shareHymn() }
        binding.buttonEdit.setOnClickListener { editHymn() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }

        loadHymn()
    }

    override fun onResume() {
        super.onResume()
        loadHymn()
    }

    private fun loadHymn() {
        val id = intent.getLongExtra(EXTRA_HYMN_ID, -1L)
        if (id == -1L) { finish(); return }
        lifecycleScope.launch {
            val h = repository.getById(id)
            if (h == null) { finish(); return@launch }
            hymn = h
            binding.textCategory.text = h.category
            binding.textTitle.text = h.title
            binding.textLyrics.text = h.lyrics
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteIcon() {
        val h = hymn ?: return
        binding.buttonFavorite.setImageResource(
            if (h.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    private fun toggleFavorite() {
        val h = hymn ?: return
        lifecycleScope.launch {
            val updated = h.copy(isFavorite = !h.isFavorite)
            repository.update(updated)
            hymn = updated
            updateFavoriteIcon()
        }
    }

    private fun shareHymn() {
        val h = hymn ?: return
        val text = "${h.title}\n\n${h.lyrics}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, h.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, h.title))
    }

    private fun editHymn() {
        val h = hymn ?: return
        val intent = Intent(this, AddEditHymnActivity::class.java)
        intent.putExtra(AddEditHymnActivity.EXTRA_HYMN_ID, h.id)
        startActivity(intent)
    }

    private fun confirmDelete() {
        val h = hymn ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                lifecycleScope.launch {
                    repository.delete(h)
                    finish()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    companion object {
        const val EXTRA_HYMN_ID = "extra_hymn_id"
    }
}
