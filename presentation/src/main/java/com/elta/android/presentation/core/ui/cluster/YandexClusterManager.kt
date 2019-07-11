package com.elta.android.presentation.core.ui.cluster

import com.a65apps.clustering.core.VisibleRect
import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.CacheNonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.view.YandexRenderConfig
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateSource
import com.yandex.mapkit.map.Map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YandexClusterManager(
    renderer: ClusterRenderer<YandexRenderConfig>,
    algorithm: Algorithm<DefaultAlgorithmParameter> = CacheNonHierarchicalDistanceBasedAlgorithm(),
    algorithmParameter: DefaultAlgorithmParameter
) :
    DefaultClusterManager<YandexRenderConfig>(
        renderer,
        algorithm,
        algorithmParameter
    ),
    CameraListener {

    private var lastZoom: Int = 0
    private val scope = CoroutineScope(Dispatchers.Map)

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        updateSource: CameraUpdateSource,
        isFinal: Boolean
    ) {
        if (!isFinal) return
        scope.coroutineContext.cancelChildren()
        scope.launch {
            val currentZoom = cameraPosition.zoom.toInt()
            if (lastZoom != currentZoom) lastZoom = currentZoom
            val visibleRect = withContext(Dispatchers.Main) {
                VisibleRect(map.visibleRegion.topLeft.toLatLng(),
                    map.visibleRegion.bottomRight.toLatLng())
            }
            val algorithmParameter = DefaultAlgorithmParameter(visibleRect, lastZoom)
            calculateClusters(algorithmParameter)
        }
    }
}