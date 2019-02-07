package com.elta.android.presentation.core.ui.fragment

import android.location.Location
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.utils.toPoint
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

abstract class BaseYandexMapFragment<T> : BaseFragment<T>() where T : BasePm {

    protected var mapView: MapView? = null
    protected var map: Map? = null
    private val myLocationImageProvider by lazy {
        ImageProvider.fromResource(activity, R.drawable.ic_my_loc)
    }

    private val mapObjects by lazy { map?.mapObjects?.addCollection() }

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

    fun moveTo(location: Location, zoom: Float? = null) {
        map?.move(
            CameraPosition(location.toPoint(), zoom ?: DEFAULT_ZOOM, AZIMUT, TILT),
            Animation(Animation.Type.SMOOTH, ANIMATION_DURATION),
            null
        )
    }

    fun addMyLocationPin(location: Location) {
        // TODO LOOKS LIKE SHIT. SHOULD BE IMPROVED
        mapObjects?.clear()
        mapObjects?.addPlacemark(location.toPoint(), myLocationImageProvider)
    }

    companion object {
        private const val DEFAULT_ZOOM = 18f
        private const val AZIMUT = 0f
        private const val TILT = 0f
        private const val ANIMATION_DURATION = 5f
    }
}