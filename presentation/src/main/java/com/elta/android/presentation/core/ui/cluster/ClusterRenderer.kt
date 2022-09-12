package com.elta.android.presentation.core.ui.cluster

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.view.RenderConfig

interface ClusterRenderer<in C : RenderConfig> {

    suspend fun updateClusters(newClusters: Set<Cluster>, updateSelection: Boolean)

    fun config(renderConfig: C)

    fun onAdd()

    fun onRemove()
}
