package com.elta.android.presentation.core.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.bindTo

abstract class BaseRecyclerViewFragment<T : BaseListPm, B : ViewBinding>(
    bindingInflater: Inflater<B>,
) : BaseFragment<T, B>(bindingInflater) {

    abstract val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder>

    protected var itemsView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView = view.findViewById(R.id.itemsView)
        itemsView?.layoutManager = provideLayoutManager()
        itemsView?.adapter = adapter
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        pm.items.bindTo(adapter::submitList)
    }

    protected open fun provideLayoutManager(): RecyclerView.LayoutManager =
        FixedLinearLayoutManager(requireContext())
}
