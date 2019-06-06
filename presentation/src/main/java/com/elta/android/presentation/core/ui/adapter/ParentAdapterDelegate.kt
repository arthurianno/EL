package com.elta.android.presentation.core.ui.adapter

import android.support.v7.widget.RecyclerView
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

abstract class ParentAdapterDelegate(
    protected val calculator: DiffCalculator,
    protected val factory: AdapterDelegatesFactory
) : AdapterDelegate() {

    protected val adapters = mutableMapOf<Any, DynamicAdapter>()

    protected open fun setItems(view: RecyclerView, useDiffUtils: Boolean, parent: ParentItem) {
        val adapter = createOrGetAdapter(parent)
        view.adapter = adapter
        adapter.updateData(parent.items, useDiffUtils)
    }

    protected open fun createOrGetAdapter(item: ListItem): DynamicAdapter = adapters[item.getUniqueProperty()]
        ?: DynamicAdapter(factory, calculator).also { adapters[item.getUniqueProperty()] = it }

}