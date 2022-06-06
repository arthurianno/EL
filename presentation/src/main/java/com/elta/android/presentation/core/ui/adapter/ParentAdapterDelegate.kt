package com.elta.android.presentation.core.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

abstract class ParentAdapterDelegate(
    protected val factory: AdapterDelegatesFactory
) : AdapterDelegate() {

    protected val adapters = mutableMapOf<Any, DynamicAdapter>()
    protected val disposables = mutableMapOf<Any, CompositeDisposable>()

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        holder.withAdapterPosition<ListItem> { _, item, _ ->
            disposables[item.getUniqueProperty()]?.clear()
        }
    }

    protected open fun setItems(view: RecyclerView, useDiffUtils: Boolean, parent: ParentItem) {
        val adapter = createOrGetAdapter(parent)
        view.adapter = adapter

        if (useDiffUtils) {
            val disposable = createOrGetCompositeDisposable(parent)
            Observable.just(parent.items).bindTo(adapter, disposable)
        } else {
            adapter.updateData(parent.items, useDiffUtils)
        }
    }

    protected open fun createOrGetAdapter(item: ListItem): DynamicAdapter =
        adapters[item.getUniqueProperty()]
            ?: DynamicAdapter(factory).also { adapters[item.getUniqueProperty()] = it }

    protected open fun createOrGetCompositeDisposable(item: ListItem): CompositeDisposable =
        disposables[item.getUniqueProperty()]
            ?: CompositeDisposable().also { disposables[item.getUniqueProperty()] = it }
}
