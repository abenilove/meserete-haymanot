package com.mezmuretewahedo.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.mezmuretewahedo.app.MezmurApp
import com.mezmuretewahedo.app.R
import com.mezmuretewahedo.app.data.Hymn
import com.mezmuretewahedo.app.data.HymnRepository
import com.mezmuretewahedo.app.data.LyricsFileImporter
import com.mezmuretewahedo.app.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: HymnRepository
    private lateinit var adapter: HymnListAdapter

    private val expandedCategories = mutableSetOf<String>()
    private var showFavoritesOnly = false
    private var currentQuery: String = ""
    private var collectJob: Job? = null
    private var lastHymns: List<Hymn> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as MezmurApp).repository

        setSupportActionBar(binding.toolbar)

        adapter = HymnListAdapter(
            onHeaderClick = ::toggleCategory,
            onHymnClick = ::openHymn,
            onFavoriteClick = ::toggleFavorite
        )
        binding.recyclerView.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showFavoritesOnly = tab.position == 1
                expandedCategories.clear()
                observeHymns()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditHymnActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener { runImport() }

        observeHymns()
    }

    private fun toggleCategory(category: String) {
        if (!expandedCategories.remove(category)) {
            expandedCategories.add(category)
        }
        renderList(lastHymns)
    }

    private fun openHymn(hymn: Hymn) {
        val intent = Intent(this, HymnDetailActivity::class.java)
        intent.putExtra(HymnDetailActivity.EXTRA_HYMN_ID, hymn.id)
        startActivity(intent)
    }

    private fun toggleFavorite(hymn: Hymn) {
        lifecycleScope.launch {
            repository.update(hymn.copy(isFavorite = !hymn.isFavorite))
        }
    }

    private fun observeHymns() {
        collectJob?.cancel()
        val flow = when {
            currentQuery.isNotBlank() -> repository.search(currentQuery)
            showFavoritesOnly -> repository.observeFavorites()
            else -> repository.observeAll()
        }
        collectJob = lifecycleScope.launch {
            flow.collectLatest { hymns ->
                lastHymns = hymns
                renderList(hymns)
            }
        }
    }

    private fun renderList(hymns: List<Hymn>) {
        val flatten = currentQuery.isNotBlank() || showFavoritesOnly
        val items = buildListItems(hymns, expandedCategories, forceFlat = flatten)
        adapter.submitList(items)
        binding.emptyState.visibility = if (hymns.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.swipeRefresh.isRefreshing = false
    }

    private fun runImport() {
        lifecycleScope.launch {
            val count = repository.importFromFolder(this@MainActivity)
            binding.swipeRefresh.isRefreshing = false
            val message = if (count > 0) {
                getString(R.string.imported_count, count)
            } else {
                getString(R.string.no_new_files)
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                observeHymns()
                return true
            }
        })

        val syncItem = menu.findItem(R.id.action_sync)
        syncItem.setOnMenuItemClickListener {
            val folder = LyricsFileImporter.importFolder(this)
            Toast.makeText(
                this,
                getString(R.string.import_folder_hint, folder.absolutePath),
                Toast.LENGTH_LONG
            ).show()
            runImport()
            true
        }

        return true
    }
}
