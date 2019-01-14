package com.nullgr.android.presentation.core.ui.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.nullgr.android.presentation.R
import com.nullgr.android.presentation.core.pm.BaseListPm
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
        pm.items.bindTo { items -> adapter.updateData(items) }
    }

    protected open fun provideLayoutManager(context: Context?): RecyclerView.LayoutManager =
        LinearLayoutManager(context)
}