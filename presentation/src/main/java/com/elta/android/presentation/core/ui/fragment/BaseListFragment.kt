package com.elta.android.presentation.core.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.ui.adapter.bindTo
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.nullgr.core.adapter.DynamicAdapter
import javax.inject.Inject

@Deprecated("Класс не используется. Можно удалить")
abstract class BaseListFragment<T : BaseListPm, B : ViewBinding>(
    bindingInflater: Inflater<B>
) : BaseFragment<T, B>(bindingInflater) {

    @Inject
    lateinit var adapter: DynamicAdapter

    protected var itemsView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView = view.findViewById(R.id.itemsView)
        itemsView?.layoutManager = provideLayoutManager()
        itemsView?.adapter = adapter
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        pm.items.observable.bindTo(adapter, compositeUnbind)
    }

    protected open fun provideLayoutManager(): RecyclerView.LayoutManager =
        FixedLinearLayoutManager(requireContext())
}
