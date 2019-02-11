package com.elta.android.presentation.features.shops.map.ui

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseYandexMapFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.shops.map.pm.ShopsMapPm
import com.elta.android.presentation.utils.applyWindowInsetsForChildrenView
import com.elta.android.presentation.utils.scrollStateChanges
import com.elta.android.presentation.widgets.MarginItemDecoration
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.textChanges
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.ui.extensions.hideKeyboard
import kotlinx.android.synthetic.main.fragment_shops_map.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import javax.inject.Inject

class ShopsMapFragment : BaseYandexMapFragment<ShopsMapPm>() {

    @Inject
    lateinit var adapter: DynamicAdapter

    @Inject
    lateinit var searchAdapter: DynamicAdapter

    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentStatusBarConfigProvider
    override val screenLayout: Int = R.layout.fragment_shops_map
    override val classToken: Class<ShopsMapPm> = ShopsMapPm::class.java

    private val snapHelper = PagerSnapHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
        toolbarTitleView.text = getString(R.string.shops_map_toolbar_title)
        toolbarView.applyWindowInsetsForChildrenView()

        itemsView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        itemsView.adapter = adapter
        itemsView.addItemDecoration(
            MarginItemDecoration(
                checkNotNull(context),
                R.dimen.shop_margin,
                R.dimen.shop_margin,
                R.dimen.shop_between
            )
        )
        snapHelper.attachToRecyclerView(itemsView)

        searchItemsView.layoutManager = LinearLayoutManager(context)
        searchItemsView.adapter = searchAdapter
//        searchItemsView.addItemDecoration(
//            DividerItemDecoration(
//                checkNotNull(context),
//                R.drawable.divider_search,
//                false
//            )
//        )
    }

    override fun onBindPresentationModel(pm: ShopsMapPm) {
        super.onBindPresentationModel(pm)
        pm.items.bindTo { items -> adapter.updateData(items) }
        pm.searchItems.bindTo { items -> searchAdapter.updateData(items) }
        pm.searchInput.bindTo(searchInputView)
        searchInputView.textChanges().map { it.isNotEmpty() }.bindTo { searchIconView.isSelected = it }
        searchInputView.textChanges().map { it.isNotEmpty() }.bindTo(searchItemsView.visibility())
        searchItemsView.scrollStateChanges()
            .filter { it != RecyclerView.SCROLL_STATE_IDLE }
            .bindTo { searchInputView.hideKeyboard() }
    }

    companion object {
        fun newInstance() = ShopsMapFragment()
    }
}
