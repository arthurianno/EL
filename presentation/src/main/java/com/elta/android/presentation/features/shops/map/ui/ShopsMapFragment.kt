package com.elta.android.presentation.features.shops.map.ui

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.geo.GeoPoint
import com.elta.android.presentation.core.permissions.requestStatus
import com.elta.android.presentation.core.permissions.statusFor
import com.elta.android.presentation.core.ui.fragment.BaseYandexMapFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.shops.map.pm.ShopsMapPm
import com.elta.android.presentation.utils.applyWindowInsetsForChildrenView
import com.elta.android.presentation.utils.toPoint
import com.elta.android.presentation.widgets.MarginItemDecoration
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.adapter.DynamicAdapter
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_shops_map.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import timber.log.Timber
import javax.inject.Inject

@Suppress("MagicNumber")
class ShopsMapFragment : BaseYandexMapFragment<ShopsMapPm>() {

    @Inject
    lateinit var adapter: DynamicAdapter

    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentStatusBarConfigProvider
    override val screenLayout: Int = R.layout.fragment_shops_map
    override val classToken: Class<ShopsMapPm> = ShopsMapPm::class.java
    override val selectedPinRes = R.drawable.ic_active_pin
    override val normalPinRes = R.drawable.ic_normal_pin
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
    }

    override fun onBindPresentationModel(pm: ShopsMapPm) {
        super.onBindPresentationModel(pm)
        myLocationButtonView.clicks().bindTo(pm.fetchMyLocationAction)

        pm.items.bindTo { items -> adapter.updateData(items) }
        pm.showMyLocationCommand.bindTo(::showUserLocation)
        pm.permissionRequiredCommand.observable
            .flatMap { rxPermissions.requestStatus(LOCATION_PERMISSION) }
            .bindTo(pm.permissionStatusUpdatedAction.consumer)
        pm.permissionStatusUpdatedAction.consumer.accept(rxPermissions.statusFor(LOCATION_PERMISSION))

        pinClicks().subscribe {
            Timber.d("onBindPresentationModel $it")
        }.untilUnbind()

        addPins(arrayListOf(
            GeoPoint(47.117953, 37.521493),
            GeoPoint(47.118359, 37.519666),
            GeoPoint(47.115393, 37.520877),
            GeoPoint(47.117465, 37.522465)
        ))
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
