package com.elta.android.presentation.features.shops.map.pm

import com.elta.android.domain.features.sale_points.interactor.GetSalePointsUseCase
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class ShopsMapPm @Inject constructor(
    private val getSalePointsUseCase: GetSalePointsUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val items = State<List<ListItem>>()
    val searchItems = State<List<ListItem>>()
    val searchInput = inputControl()

    private val loadSalePoints = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        loadSalePoints.observable
            .skipWhileInProgress()
            .flatMap { params ->
                getSalePointsUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .doOnNext {
                loadSalePoints.consumer.accept(Unit)
            }
            .subscribe()
            .untilDestroy()
    }

    private fun handleSuccess(points: List<SalePoint>) {
        items.consumer.accept(points.map { it.toItem() })
    }

    private fun SalePoint.toItem(): ListItem =
        ShopItem(
            id = id,
            name = name,
            address = fullAddress,
            distance = resources.getString(R.string.shops_map_distance_km_pattern, 10)
        )
}