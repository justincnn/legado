package io.legado.app.ui.sourceedit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import io.legado.app.R
import io.legado.app.base.BaseActivity
import io.legado.app.data.entities.BookSource
import io.legado.app.utils.getViewModel
import kotlinx.android.synthetic.main.activity_source_edit.*
import org.jetbrains.anko.toast

class SourceEditActivity : BaseActivity<SourceEditViewModel>() {
    override val viewModel: SourceEditViewModel
        get() = getViewModel(SourceEditViewModel::class.java)
    override val layoutID: Int
        get() = R.layout.activity_source_edit

    private val adapter = SourceEditAdapter()
    private val sourceEditEntities: ArrayList<SourceEditEntity> = ArrayList()

    override fun onViewModelCreated(viewModel: SourceEditViewModel, savedInstanceState: Bundle?) {
        initRecyclerView()
        viewModel.sourceLiveData.observe(this, Observer {
            upRecyclerView(it)
        })
        if (viewModel.sourceLiveData.value == null) {
            val sourceID = intent.getStringExtra("data")
            if (sourceID == null) {
                upRecyclerView(null)
            } else {
                sourceID.let { viewModel.setBookSource(sourceID) }
            }
        } else {
            upRecyclerView(viewModel.sourceLiveData.value)
        }
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.source_edit, menu)
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                val bookSource = getSource()
                if (bookSource == null) {
                    toast("书源名称和URL不能为空")
                } else {
                    viewModel.save(bookSource) { finish() }
                }
            }
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
    }

    private fun upRecyclerView(bookSource: BookSource?) {
        bookSource?.let {
            cb_is_enable.isChecked = it.enabled
            cb_is_enable_find.isChecked = it.enabledExplore
        }
        sourceEditEntities.clear()
        sourceEditEntities.add(SourceEditEntity("origin", bookSource?.bookSourceUrl, R.string.book_source_url))
        sourceEditEntities.add(SourceEditEntity("name", bookSource?.bookSourceName, R.string.book_source_name))
        sourceEditEntities.add(SourceEditEntity("group", bookSource?.bookSourceGroup, R.string.book_source_group))

        adapter.sourceEditEntities = sourceEditEntities
        adapter.notifyDataSetChanged()
    }

    private fun getSource(): BookSource? {
        val bookSource = BookSource()
        bookSource.enabled = cb_is_enable.isChecked
        bookSource.enabledExplore = cb_is_enable_find.isChecked
        for (entity in adapter.sourceEditEntities) {
            when (entity.key) {
                "origin" -> {
                    if (entity.value == null) {
                        return null
                    } else {
                        bookSource.bookSourceUrl = entity.value!!
                    }
                }
                "name" -> {
                    if (entity.value == null) {
                        return null
                    } else {
                        bookSource.bookSourceName = entity.value!!
                    }
                }
                "group" -> bookSource.bookSourceGroup = entity.value
            }
        }
        return bookSource
    }

    class SourceEditEntity(var key: String, var value: String?, var hint: Int)
}