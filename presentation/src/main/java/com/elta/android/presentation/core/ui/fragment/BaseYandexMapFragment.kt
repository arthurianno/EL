package com.elta.android.presentation.core.ui.fragment

import android.location.Location
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.geo.GeoPoint
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.utils.toPoint
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nullgr.core.rx.asConsumer
import com.nullgr.core.rx.asObservable
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

abstract class BaseYandexMapFragment<T> : BaseFragment<T>(), MapObjectTapListener where T : BasePm {

    protected var mapView: MapView? = null
    protected var map: Map? = null

    protected abstract val userLocationPinRes: Int

    private val myLocationImageProvider by lazy {
        ImageProvider.fromResource(activity, userLocationPinRes)
    }
    private val mapObjects by lazy { map?.mapObjects?.addCollection() }
    private var userLocationMapObject: MapObject? = null
    private var pinObjects = hashMapOf<GeoPoint, MapObject?>()
    private val selectedObjectRelay = BehaviorRelay.create<GeoPoint>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(activity)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.yandexMapView)
        map = mapView?.map
        map?.isRotateGesturesEnabled = false
        mapObjects?.addTapListener(this)
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

    fun moveTo(location: Point, zoom: Float? = null) {
        map?.move(
            CameraPosition(location, zoom ?: DEFAULT_ZOOM, AZIMUT, TILT),
            Animation(Animation.Type.SMOOTH, ANIMATION_DURATION),
            null
        )
    }

    fun addMyLocationPin(location: Location) {
        userLocationMapObject?.let { mapObjects?.remove(it) }
        userLocationMapObject = mapObjects?.addPlacemark(location.toPoint(), myLocationImageProvider)
        userLocationMapObject?.userData = GeoPoint(location.latitude, location.longitude, isUserPoint = true)
    }

    fun replacePins(points: List<GeoPoint>) {
        clearAllPins()
        addPins(points)
    }

    fun addPins(points: List<GeoPoint>) {
        points.forEach { drawPinObject(it) }
    }

    fun selectPin(geoPoint: GeoPoint) {
        processPinSelection(geoPoint)
    }

    fun pinClicks() = selectedObjectRelay.asObservable()

    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val selectedPoint = mapObject.userData as? GeoPoint
        processPinSelection(selectedPoint)
        return true
    }

    private fun drawPinObject(geoPoint: GeoPoint) {
        if (geoPoint in pinObjects.keys) {
            pinObjects[geoPoint]?.let {
                mapObjects?.remove(it)
                pinObjects.remove(geoPoint)
            }
        }

        geoPoint.icon?.let { icon ->
            val imageProvider = when (geoPoint.selected) {
                true -> ImageProvider.fromResource(activity, icon.selected)
                else -> ImageProvider.fromResource(activity, icon.normal)
            }

            val mapObject = mapObjects?.addPlacemark(geoPoint.toPoint(), imageProvider)
            mapObject?.userData = geoPoint
            pinObjects[geoPoint] = mapObject
        }
    }

    private fun clearAllPins() {
        pinObjects.forEach { entry ->
            entry.value?.let { mapObjects?.remove(it) }
        }
        pinObjects.clear()
    }

    private fun processPinSelection(selectedPoint: GeoPoint?) {
        val previousSelectedPoint = selectedObjectRelay.value
        previousSelectedPoint?.let {
            it.selected = false
            drawPinObject(it)
        }
        selectedPoint?.let {
            if (!it.isUserPoint) {
                it.selected = true
                setSelectedPin(it)
            }
        }
    }

    private fun setSelectedPin(geoPoint: GeoPoint) {
        selectedObjectRelay.asConsumer().accept(geoPoint)
        drawPinObject(geoPoint)
        moveTo(geoPoint.toPoint())
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val AZIMUT = 0f
        private const val TILT = 0f
        private const val ANIMATION_DURATION = 3f
    }
}