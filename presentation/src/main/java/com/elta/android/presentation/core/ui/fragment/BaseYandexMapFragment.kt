package com.elta.android.presentation.core.ui.fragment

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView

abstract class BaseYandexMapFragment<T> : BaseFragment<T>() where T : BasePm {

    protected var mapView: MapView? = null
    protected var map: Map? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(activity)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.yandexMapView)
        map = mapView?.map
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
    }
}