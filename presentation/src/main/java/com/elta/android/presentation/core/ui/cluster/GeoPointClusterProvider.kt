package com.elta.android.presentation.core.ui.cluster

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.algorithm.ClusterProvider
import com.elta.android.presentation.core.geo.GeoPoint

class GeoPointClusterProvider : ClusterProvider {

    override fun get(cluster: Cluster): Cluster =
        GeoPoint(
            latitude = cluster.geoCoor().latitude,
            longitude = cluster.geoCoor().longitude,
            id = cluster.payload()
        )
}