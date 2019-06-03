package com.elta.android.presentation.features.shops.map.ui

import android.Manifest
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.permissions.requestStatus
import com.elta.android.presentation.core.permissions.statusFor
import com.elta.android.presentation.core.pm.widgets.bindTo
import com.elta.android.presentation.core.pm.widgets.resolveResults
import com.elta.android.presentation.core.ui.fragment.BaseYandexMapFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.shops.map.pm.ShopsMapPm
import com.elta.android.presentation.utils.applyWindowInsetsForChildrenView
import com.elta.android.presentation.utils.pageScrolled
import com.elta.android.presentation.utils.scrollStateChanges
import com.elta.android.presentation.utils.toPoint
import com.elta.android.presentation.widgets.decoration.MarginItemDecoration
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.ui.extensions.hideKeyboard
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_shops_map.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import javax.inject.Inject

@Suppress("MagicNumber")
class ShopsMapFragment : BaseYandexMapFragment<ShopsMapPm>() {

    @Inject
    lateinit var adapter: DynamicAdapter

    @Inject
    lateinit var searchAdapter: DynamicAdapter

    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentStatusBarConfigProvider
    override val screenLayout: Int = R.layout.fragment_shops_map
    override val classToken: Class<ShopsMapPm> = ShopsMapPm::class.java
    override val userLocationPinRes = R.drawable.ic_my_loc

    private val snapHelper = PagerSnapHelper()
    private val rxPermissions by lazy { RxPermissions(this) }

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
    }

    override fun onBindPresentationModel(pm: ShopsMapPm) {
        super.onBindPresentationModel(pm)
        myLocationButtonView.clicks().bindTo(pm.moveToMyLocationAction)
        pm.items.bindTo { items -> adapter.updateData(items) }
        pm.showMyLocationCommand.bindTo(::showUserLocation)
        pm.showDefaultLocationCommand.bindTo { moveTo(it.toPoint()) }

        pm.checkPermissionStatusCommand.bindTo {
            val status = rxPermissions.statusFor(LOCATION_PERMISSION)
            pm.setPermissionStatus(status)
        }

        pm.requestPermissionCommand.observable
            .flatMap { rxPermissions.requestStatus(LOCATION_PERMISSION) }
            .bindTo { status -> pm.setPermissionStatus(status) }

        pm.geoPoints.bindTo(::addPins)

        pm.selectGeoPointCommand.bindTo(::selectPin)
        pm.selectShopItemCommand.bindTo { itemsView.smoothScrollToPosition(it) }

        pinClicks().skip(1).bindTo(pm.shopItemGeoPointSelectedAction)
        itemsView.pageScrolled().bindTo(pm.shopListItemSelectedAction)

        // search
        pm.searchItems.bindTo { items -> searchAdapter.updateData(items) }
        pm.searchInput.bindTo(searchInputView)
        pm.searchCloseCommand.bindTo {
            searchInputView.hideKeyboard()
            activity?.window?.decorView?.clearFocus()
        }
        searchClearView.clicks().bindTo(pm.searchClearAction)
        searchInputView.textChanges().map { it.isNotEmpty() }.bindTo { searchIconView.isSelected = it }
        searchItemsView.scrollStateChanges()
            .filter { it != RecyclerView.SCROLL_STATE_IDLE }
            .bindTo { searchInputView.hideKeyboard() }

        pm.locationControl.bindTo(compositeUnbind, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presentationModel.locationControl.resolveResults(requestCode, resultCode)
    }

    private fun showUserLocation(location: Location) {
        moveTo(location.toPoint())
        addMyLocationPin(location)
    }

    companion object {
        fun newInstance() = ShopsMapFragment()
        private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }
}
