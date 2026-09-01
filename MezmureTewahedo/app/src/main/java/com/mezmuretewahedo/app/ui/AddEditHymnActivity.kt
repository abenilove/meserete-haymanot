package com.mezmuretewahedo.app.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mezmuretewahedo.app.MezmurApp
import com.mezmuretewahedo.app.data.Hymn
import com.mezmuretewahedo.app.data.HymnRepository
import com.mezmuretewahedo.app.databinding.ActivityAddEditHymnBinding
import kotlinx.coroutines.launch

class AddEditHymnActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditHymnBinding
    private lateinit var repository: HymnRepository
    private var editingHymn: Hymn? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditHymnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as MezmurApp).repository

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        lifecycleScope.launch {
            val categories = repository.allCategories()
            val adapter = ArrayAdapter(this@AddEditHymnActivity, android.R.layout.simple_list_item_1, categories)
            binding.editCategory.setAdapter(adapter)
        }

        val id = intent.getLongExtra(EXTRA_HYMN_ID, -1L)
        if (id != -1L) {
            lifecycleScope.launch {
                val h = repository.getById(id)
                if (h != null) {
                    editingHymn = h
                    binding.editTitle.setText(h.title)
                    binding.editCategory.setText(h.category, false)
                    binding.editLyrics.setText(h.lyrics)
                }
            }
        }

        binding.buttonCancel.setOnClickListener { finish() }
        binding.buttonSave.setOnClickListener { save() }
    }

    private fun save() {
        val title = binding.editTitle.text?.toString()?.trim().orEmpty()
        val category = binding.editCategory.text?.toString()?.trim().orEmpty()
        val lyrics = binding.editLyrics.text?.toString()?.trim().orEmpty()

        if (title.isEmpty()) {
            binding.layoutTitle.error = getString(com.mezmuretewahedo.app.R.string.field_title)
            return
        }
        if (lyrics.isEmpty()) {
            binding.layoutLyrics.error = getString(com.mezmuretewahedo.app.R.string.field_lyrics)
            return
        }

        lifecycleScope.launch {
            val existing = editingHymn
            if (existing != null) {
                repository.update(
                    existing.copy(
                        title = title,
                        category = category.ifBlank { "የተጨመሩ መዝሙራት" },
                        lyrics = lyrics
                    )
                )
            } else {
                repository.add(
                    Hymn(
                        title = title,
                        category = category.ifBlank { "የተጨመሩ መዝሙራት" },
                        lyrics = lyrics,
                        isUserAdded = true
                    )
                )
            }
            Toast.makeText(this@AddEditHymnActivity, title, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_HYMN_ID = "extra_hymn_id"
    }
}
