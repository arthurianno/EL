package com.elta.android.presentation.core.ui.cluster

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.DefaultCluster
import com.a65apps.clustering.core.DefaultClusterProvider
import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.algorithm.ClusterProvider
import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter
import com.a65apps.clustering.core.algorithm.GridBasedAlgorithm
import com.a65apps.clustering.core.algorithm.PROJECTION
import com.a65apps.clustering.core.geometry.Bounds
import com.a65apps.clustering.core.geometry.Point
import com.a65apps.clustering.core.projection.SphericalMercatorProjection
import com.a65apps.clustering.core.quadtree.PointQuadTree

class ViewBasedGridAlgorithm(
    private val clusterProvider: ClusterProvider = DefaultClusterProvider()
) : GridBasedAlgorithm() {

    private var parameter: DefaultAlgorithmParameter? = null
    private val quadItems: MutableSet<QuadItem> = mutableSetOf()
    private val quadTree = PointQuadTree<QuadItem>(0.0, 1.0, 0.0, 1.0)
    private val gridSize = DEFAUT_GRID_SIZE

    override fun addItem(item: Cluster) {
        val quadItem = QuadItem(item as DefaultCluster)
        synchronized(quadTree) {
            quadItems.add(quadItem)
            quadTree.add(quadItem)
        }
    }

    override fun addItems(items: Collection<Cluster>) {
        items.forEach { addItem(it) }
    }

    override fun clearItems() {
        synchronized(quadTree) {
            quadItems.clear()
            quadTree.clear()
        }
    }

    override fun removeItem(item: Cluster) {
        val quadItem = QuadItem(item as DefaultCluster)
        synchronized(quadTree) {
            quadItems.remove(quadItem)
            quadTree.remove(quadItem)
        }
    }

    override fun removeItems(items: Collection<Cluster>) {
        synchronized(quadTree) {
            items.forEach {
                val quadItem = QuadItem(it as DefaultCluster)
                quadItems.remove(quadItem)
                quadTree.remove(quadItem)
            }
        }
    }

    @Suppress("MagicNumber")
    override fun calculate(parameter: DefaultAlgorithmParameter): Set<Cluster> {
        this.parameter = parameter
        val numCells = Math.ceil(256 * Math.pow(2.0, parameter.zoom.toDouble()) / gridSize).toLong()
        val proj = SphericalMercatorProjection(numCells.toDouble())

        val clusters = mutableSetOf<Cluster>()
        val results = mutableSetOf<Cluster>()

        val sparseArray = mutableMapOf<Long, Cluster>()

        synchronized(quadTree) {
            val clusterItems = quadTree.search(visibleBounds())
            for (item in clusterItems) {
                val p = proj.toPoint(item.cluster.geoCoor())
                val coord = getCoord(numCells, p.x, p.y)
                var cluster = sparseArray[coord]
                if (cluster == null) {
                    val latLng = getLatLng(proj, p)
                    cluster = clusterProvider.get(DefaultCluster(latLng))
                    sparseArray[coord] = cluster
                    clusters.add(cluster)
                }
                cluster.addItem(item.cluster)
            }
            clusters.forEach {
                if (it.isCluster()) {
                    results.add(it)
                } else {
                    results.addAll(it.items())
                }
            }
        }
        return results
    }

    private fun getLatLng(proj: SphericalMercatorProjection, point: Point): LatLng =
        proj.toLatLng(point)

    private fun getCoord(numCells: Long, x: Double, y: Double): Long =
        (numCells * Math.floor(x) + Math.floor(y)).toLong()

    @Suppress("UnsafeCallOnNullableType")
    private fun visibleBounds(): Bounds {
        val rect = parameter!!.visibleRect
        val topLeft = PROJECTION.toPoint(rect.topLeft)
        val bottomRight = PROJECTION.toPoint(rect.bottomRight)
        val minX = topLeft.x
        val maxX = bottomRight.x
        val minY = topLeft.y
        val maxY = bottomRight.y
        val delta = (maxX - minX) / 2
        return Bounds(minX - delta, maxX + delta, minY - delta, maxY + delta)
    }

    private companion object {
        private const val DEFAUT_GRID_SIZE = 100
    }
}

class QuadItem(val cluster: Cluster) : PointQuadTree.Item {

    private val position: LatLng = cluster.geoCoor()

    override val point: Point = PROJECTION.toPoint(position)

    override fun hashCode(): Int = cluster.hashCode()

    override fun equals(other: Any?): Boolean =
        (other as? QuadItem)?.cluster?.equals(cluster) ?: false
}
