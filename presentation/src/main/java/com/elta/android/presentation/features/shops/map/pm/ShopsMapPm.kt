package com.elta.android.presentation.features.shops.map.pm

import com.elta.android.domain.features.sale_points.interactor.GetSalePointsUseCase
import com.elta.android.domain.features.sale_points.interactor.SearchSalePointsUseCase
import com.elta.android.domain.features.sale_points.interactor.isSearchInputValid
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.adapter.CardType
import com.elta.android.presentation.core.ui.adapter.getCardType
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchHeaderItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.bindProgress
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.widget.inputControl
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShopsMapPm @Inject constructor(
    private val searchSalePointsUseCase: SearchSalePointsUseCase,
    private val getSalePointsUseCase: GetSalePointsUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val items = State<List<ListItem>>()
    val searchItems = State<List<ListItem>>()
    val searchInput = inputControl()
    val searchAction = Action<String>()
    val searchProgressState = State(false)
    val searchClearAction = Action<Unit>()
    val searchCloseCommand = Command<Unit>()

    private val loadSalePoints = Action<Unit>()

    @Suppress("LongMethod")
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

        searchInput.text.observable
            .debounce(INPUT_DELAY, TimeUnit.MILLISECONDS)
            .doOnNext { query ->
                if (!isSearchInputValid(query)) {
                    searchItems.consumer.accept(emptyList())
                } else {
                    searchAction.consumer.accept(query)
                }
            }
            .subscribe()
            .untilDestroy()

        searchAction.observable
            .skipWhileInProgress(progressState.observable)
            .map(::createSearchParams)
            .flatMap { params ->
                searchSalePointsUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress(progressState.consumer)
                    .doOnNext(::handleSearchSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        searchClearAction.observable
            .subscribe {
                when (searchInput.text.value.isEmpty()) {
                    true -> {
                        searchItems.consumer.accept(emptyList())
                        searchCloseCommand.consumer.accept(Unit)
                    }
                    else -> searchInput.textChanges.consumer.accept("")
                }
            }
            .untilDestroy()

        bus.clicks<Clicks.SearchResult>()
            .subscribe {
                searchInput.textChanges.consumer.accept("")
                searchCloseCommand.consumer.accept(Unit)
                searchItems.consumer.accept(emptyList())

                // TODO: navigate to sale point on map and in the list
            }
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .doOnNext {
                loadSalePoints.consumer.accept(Unit)
            }
            .subscribe()
            .untilDestroy()
    }

    private fun createSearchParams(query: String): SearchSalePointsUseCase.Params =
        SearchSalePointsUseCase.Params(query)

    private fun handleSearchSuccess(points: List<SalePoint>) {
        if (points.isEmpty()) {
            searchItems.consumer.accept(emptyList())
        } else {
            val result = mutableListOf<ListItem>()
            result.add(SearchHeaderItem())
            points.forEach { point ->
                result.add(point.toSearchItem(getCardType(points.size + 1, result.size)))
            }
            searchItems.consumer.accept(result)
        }
    }

    private fun handleSuccess(points: List<SalePoint>) {
        items.consumer.accept(points.map { it.toItem() })
    }

    @Suppress("MagicNumber")
    private fun SalePoint.toItem(): ListItem =
        ShopItem(
            id = id,
            name = name,
            address = "$city, $address",
            distance = resources.getString(R.string.shops_map_distance_km_pattern, 10)
        )

    @Suppress("MagicNumber")
    private fun SalePoint.toSearchItem(cardType: CardType): ListItem =
        SearchResultItem(
            id = id,
            name = name,
            address = "$city, $address",
            cardType = cardType
        )

    private companion object {
        const val INPUT_DELAY = 300L
    }
}