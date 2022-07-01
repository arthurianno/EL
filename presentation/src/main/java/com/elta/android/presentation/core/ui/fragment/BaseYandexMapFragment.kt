package com.elta.android.presentation.core.ui.fragment

import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.viewbinding.ViewBinding
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.VisibleRect
import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.TapListener
import com.a65apps.clustering.yandex.view.YandexRenderConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.core.geo.GeoPoint
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.ui.cluster.GeoPointClusterProvider
import com.elta.android.presentation.core.ui.cluster.ViewBasedGridAlgorithm
import com.elta.android.presentation.core.ui.cluster.YandexClusterManager
import com.elta.android.presentation.core.ui.cluster.YandexClusterRenderer
import com.elta.android.presentation.utils.distanceTo
import com.elta.android.presentation.utils.toPoint
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nullgr.core.rx.asConsumer
import com.nullgr.core.rx.asObservable
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBoxHelper
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

abstract class BaseYandexMapFragment<T : BasePm, B : ViewBinding>(
    bindingInflater: Inflater<B>
) : BaseFragment<T, B>(bindingInflater) {

    protected abstract val userLocationPinRes: Int
    protected abstract val clusterPinProvider: ClusterPinProvider

    protected var mapView: MapView? = null
    protected var map: Map? = null

    private var clusterManager: YandexClusterManager? = null
    private val myLocationImageProvider by lazy {
        ImageProvider.fromResource(activity, userLocationPinRes)
    }
    private val mapObjects by lazy { map?.addMapObjectLayer("USER_LOCATION") }
    private var userLocationMapObject: MapObject? = null
    private val selectedObjectRelay = BehaviorRelay.create<GeoPoint>()

    private val tapListener = object : TapListener {
        override fun clusterTapped(cluster: Cluster, mapObject: PlacemarkMapObject) {
            cluster as GeoPoint
            if (cluster.size() == 1) {
                processPinSelection(cluster)
            } else {
                moveTo(cluster.toPoint(), increaseCLusterZoom(), CLUSTER_ANIMATION_DURATION)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(activity)
        super.onActivityCreated(savedInstanceState)
        initClusterManager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.yandexMapView)
        map = mapView?.map
        map?.isRotateGesturesEnabled = false
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

    fun moveTo(
        location: Point,
        zoom: Float? = null,
        duration: Float? = null
    ) {
        val finalZoom = zoom ?: map?.maxZoom ?: DEFAULT_ZOOM
        map?.move(
            CameraPosition(location, finalZoom, AZIMUT, TILT),
            Animation(Animation.Type.SMOOTH, duration ?: PIN_ANIMATION_DURATION),
            null
        )
    }

    fun addMyLocationPin(location: Location) {
        userLocationMapObject?.let { mapObjects?.remove(it) }
        userLocationMapObject =
            mapObjects?.addPlacemark(location.toPoint(), myLocationImageProvider)
        userLocationMapObject?.userData =
            GeoPoint(location.latitude, location.longitude, isUserPoint = true)
    }

    fun replacePins(points: List<GeoPoint>) {
        clearAllPins()
        addPins(points)
    }

    fun addPins(points: List<GeoPoint>) {
        clusterManager?.addItems(points.toSet())
    }

    fun selectPin(geoPoint: GeoPoint, isMoveToPin: Boolean) {
        processPinSelection(geoPoint, isMoveToPin)
    }

    fun pinClicks() = selectedObjectRelay.asObservable()

    fun moveToPointsInBounds(points: List<Point>) {
        map?.let { nonNullMap ->
            val boundingBox = BoundingBoxHelper.getBounds(Polyline(points))
            var cameraPosition = nonNullMap.cameraPosition(boundingBox)
            cameraPosition = CameraPosition(
                cameraPosition.target,
                cameraPosition.zoom - ZOOM_DIFF,
                cameraPosition.azimuth,
                cameraPosition.tilt
            )
            nonNullMap.move(
                cameraPosition,
                Animation(Animation.Type.SMOOTH, CLUSTER_ANIMATION_DURATION),
                null
            )
        }
    }

    private fun initClusterManager() {
        map?.let { m ->
            val renderer = YandexClusterRenderer(
                m,
                clusterPinProvider,
                YandexRenderConfig(interpolator = AccelerateDecelerateInterpolator()),
                tapListener
            )
            val parameter = DefaultAlgorithmParameter(
                VisibleRect(
                    m.visibleRegion.topLeft.toLatLng(),
                    m.visibleRegion.bottomRight.toLatLng()
                ),
                m.cameraPosition.zoom.toInt()
            )
            clusterManager = YandexClusterManager(
                renderer,
                ViewBasedGridAlgorithm(GeoPointClusterProvider()),
                parameter
            )
            clusterManager?.let { m.addCameraListener(it) }
        }
    }

    private fun drawPinObject(geoPoint: GeoPoint) {
        geoPoint.icon?.let { clusterManager?.addItem(geoPoint) }
    }

    private fun clearAllPins() {
        clusterManager?.clearItems()
    }

    private fun processPinSelection(selectedPoint: GeoPoint?, isMoveToPin: Boolean = true) {
        val previousSelectedPoint = getSelectedGeoPoint()
        if (previousSelectedPoint?.id == selectedPoint?.id &&
            previousSelectedPoint?.selected == selectedPoint?.selected
        ) return

        previousSelectedPoint?.let {
            clusterManager?.removeItem(it)
            it.selected = false
            drawPinObject(it)
        }
        selectedPoint?.let {
            if (!it.isUserPoint) {
                clusterManager?.removeItem(it)
                it.selected = true
                setSelectedPin(it, isMoveToPin, checkAnimationNeeded(it))
            }
        }
    }

    private fun checkAnimationNeeded(point: GeoPoint) =
        map?.let { nonNullMap ->
            point.distanceTo(nonNullMap.cameraPosition.target) < POINT_ANIMATION_THRESHOLD
        } ?: false

    private fun setSelectedPin(geoPoint: GeoPoint, isMoveToPin: Boolean, withAnimation: Boolean) {
        selectedObjectRelay.asConsumer().accept(geoPoint)
        drawPinObject(geoPoint)
        if (isMoveToPin) moveTo(
            location = geoPoint.toPoint(),
            duration = if (withAnimation) null else 0f
        )
    }

    private fun getSelectedGeoPoint() = selectedObjectRelay.value

    private fun increaseCLusterZoom() =
        (map?.cameraPosition?.zoom ?: DEFAULT_ZOOM) + ZOOM_INCREASE_VALUE

    companion object {
        private const val DEFAULT_ZOOM = 17f
        private const val ZOOM_INCREASE_VALUE = 1f
        private const val AZIMUT = 0f
        private const val TILT = 0f
        private const val PIN_ANIMATION_DURATION = 1f
        private const val CLUSTER_ANIMATION_DURATION = 1f
        private const val POINT_ANIMATION_THRESHOLD = 100000f // meters
        private const val ZOOM_DIFF = 0.3f
    }
}
