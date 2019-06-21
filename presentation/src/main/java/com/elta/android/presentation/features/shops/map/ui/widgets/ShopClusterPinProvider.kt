package com.elta.android.presentation.features.shops.map.ui.widgets

import android.content.Context
import android.support.annotation.DrawableRes
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.YandexPinProvider
import com.elta.android.presentation.core.geo.GeoPoint
import com.elta.android.presentation.features.shops.map.ui.widgets.PinProviderType.ClusterProvider
import com.elta.android.presentation.features.shops.map.ui.widgets.PinProviderType.SinglePinProvider
import com.elta.android.presentation.features.shops.map.ui.widgets.PinProviderType.SinglePinProvider.NormalPinProvider
import com.elta.android.presentation.features.shops.map.ui.widgets.PinProviderType.SinglePinProvider.SelectedPinProvider
import com.yandex.runtime.image.ImageProvider

class ShopClusterPinProvider(private val context: Context) : ClusterPinProvider {

    private val providers = mutableMapOf<PinProviderType?, YandexPinProvider>()

    private var selectedPinProvider: SelectedPinProvider? = null
    private var normalPinProvider: NormalPinProvider? = null
    private var clusterProvider: ClusterProvider? = null
    private val clusterView: ShopClusterPinView by lazy { ShopClusterPinView(context) }

    override fun get(cluster: Cluster): YandexPinProvider {
        cluster as GeoPoint
        val size = cluster.size()

        if (normalPinProvider == null || normalPinProvider?.icon == INCORRECT_VALUE) {
            initNormalPinProvider(cluster.icon?.normal)
        }

        if (selectedPinProvider == null || selectedPinProvider?.icon == INCORRECT_VALUE) {
            initSelectedPinProvider(cluster.icon?.selected)
        }

        val type = when {
            cluster.selected && size == DEFAULT_SINGLE -> selectedPinProvider
            !cluster.selected && size == DEFAULT_SINGLE -> normalPinProvider
            else -> clusterProvider?.apply { this.size = size }
        }

        return providers[type]?.let { it } ?: createClusterProvider(type ?: ClusterProvider(size))
    }

    private fun initSelectedPinProvider(@DrawableRes icon: Int?) {
        selectedPinProvider = createSelectedPinProvider(icon)
        selectedPinProvider?.let { provider ->
            createSinglePinProvider(provider)?.let { providers[selectedPinProvider] = it }
        }
    }

    private fun initNormalPinProvider(@DrawableRes icon: Int?) {
        normalPinProvider = createNormalPinProvider(icon)
        normalPinProvider?.let { provider ->
            createSinglePinProvider(provider)?.let { providers[normalPinProvider] = it }
        }
    }

    private fun createClusterProvider(type: PinProviderType): YandexPinProvider {
        clusterView.setText(type.size.toString())
        val provider = YandexPinProvider.from(
            ImageProvider.fromBitmap(clusterView.getBitmap())
        )
        providers[type] = provider
        return provider
    }

    private fun createSinglePinProvider(type: SinglePinProvider): YandexPinProvider? =
        YandexPinProvider.from(
            ImageProvider.fromResource(
                context,
                type.icon,
                true
            )
        )

    private fun createSelectedPinProvider(@DrawableRes icon: Int?) =
        SelectedPinProvider(DEFAULT_SINGLE, icon ?: INCORRECT_VALUE)

    private fun createNormalPinProvider(@DrawableRes icon: Int?) =
        NormalPinProvider(DEFAULT_SINGLE, icon ?: INCORRECT_VALUE)

    private companion object {
        private const val INCORRECT_VALUE = -1
        private const val DEFAULT_SINGLE = 1
    }
}

sealed class PinProviderType {
    abstract var size: Int

    sealed class SinglePinProvider : PinProviderType() {
        abstract val icon: Int

        data class SelectedPinProvider(
            override var size: Int,
            @DrawableRes override val icon: Int
        ) : SinglePinProvider()

        data class NormalPinProvider(
            override var size: Int,
            @DrawableRes override val icon: Int
        ) : SinglePinProvider()
    }

    data class ClusterProvider(override var size: Int) : PinProviderType()
}