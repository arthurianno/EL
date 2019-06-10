package com.elta.android.presentation.core.ui.cluster

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter
import com.a65apps.clustering.core.view.RenderConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

open class DefaultClusterManager<in C : RenderConfig>(
    private val renderer: ClusterRenderer<C>,
    private var algorithm: Algorithm<DefaultAlgorithmParameter>,
    private var algorithmParameter: DefaultAlgorithmParameter
) : ClusterManager {

    init {
        renderer.onAdd()
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val algorithmLock = ReentrantReadWriteLock()

    suspend fun calculateClusters(algorithmParameter: DefaultAlgorithmParameter) {
        this.algorithmParameter = algorithmParameter
        calculateClusters()
    }

    private suspend fun calculateClusters(updateSelection: Boolean = false) {
        val newItems = calculateNewItems()
        callRenderer(newItems, updateSelection)
    }

    private fun calculateNewItems(): Set<Cluster> =
        algorithm.calculate(algorithmParameter)

    override fun clearItems() {
        algorithmLock.writeLock().withLock {
            algorithm.clearItems()
            onModifyRawClusters()
        }
    }

    override fun setItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.addItems(clusters)
            onModifyRawClusters()
        }
    }

    override fun addItem(cluster: Cluster) {
        algorithmLock.writeLock().withLock {
            algorithm.addItem(cluster)
            onModifyRawClusters(true)
        }
    }

    override fun removeItem(cluster: Cluster) {
        algorithmLock.writeLock().withLock {
            algorithm.removeItem(cluster)
            onModifyRawClusters()
        }
    }

    override fun addItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.addItems(clusters)
            onModifyRawClusters()
        }
    }

    override fun removeItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.removeItems(clusters)
            onModifyRawClusters()
        }
    }

    private suspend fun callRenderer(newClusters: Set<Cluster>, updateSelection: Boolean) {
        renderer.updateClusters(newClusters, updateSelection)
    }

    private fun onModifyRawClusters(updateSelection: Boolean = false) {
        scope.coroutineContext.cancelChildren()
        scope.launch { calculateClusters(updateSelection) }
    }
}
