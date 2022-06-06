package com.elta.android.presentation.core.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.ui.adapter.bindTo
import com.nullgr.core.adapter.DynamicAdapter
import javax.inject.Inject

abstract class BaseListActivity<T : BaseListPm> : BaseActivity<T>() {

    @Inject
    lateinit var adapter: DynamicAdapter

    protected var itemsView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemsView = findViewById(R.id.itemsView)
        itemsView?.layoutManager = provideLayoutManager(this)
        itemsView?.adapter = adapter
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        pm.items.observable.bindTo(adapter, compositeUnbind)
    }

    protected open fun provideLayoutManager(context: Context?): RecyclerView.LayoutManager =
        LinearLayoutManager(context)
}
