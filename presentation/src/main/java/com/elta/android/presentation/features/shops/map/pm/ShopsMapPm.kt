package com.elta.android.presentation.features.shops.map.pm

import android.annotation.SuppressLint
import android.location.Location
import com.elta.android.domain.features.sale_points.interactor.GetSalePointsUseCase
import com.elta.android.domain.features.sale_points.interactor.SearchSalePointsUseCase
import com.elta.android.domain.features.sale_points.interactor.isSearchInputValid
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.sale_points.model.Type
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.geo.GeoPoint
import com.elta.android.presentation.core.geo.GeoPointIcon
import com.elta.android.presentation.core.geo.emptyGeoPoint
import com.elta.android.presentation.core.geo.isEmpty
import com.elta.android.presentation.core.permissions.PermissionStatus
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.adapter.CardType
import com.elta.android.presentation.core.ui.adapter.getCardType
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchHeaderItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.elta.android.presentation.utils.distanceTo
import com.elta.android.presentation.utils.formatDistance
import com.elta.android.presentation.utils.moskowLocation
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.bindProgress
import com.nullgr.core.rx.location.EMPTY_LOCATION
import com.nullgr.core.rx.location.RxLocationManager
import com.nullgr.core.rx.location.isEmpty
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.widget.inputControl
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShopsMapPm @Inject constructor(
    private val rxLocationManager: RxLocationManager,
    private val searchSalePointsUseCase: SearchSalePointsUseCase,
    private val getSalePointsUseCase: GetSalePointsUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val items = State<List<ListItem>>()
    val geoPoints = State<List<GeoPoint>>()

    val checkPermissionStatusCommand = Command<Unit>(bufferSize = 1)
    val requestPermissionCommand = Command<Unit>(bufferSize = 1)

    val showMyLocationCommand = Command<Location>()
    val showDefaultLocationCommand = Command<Location>()
    val moveToMyLocationAction = Action<Unit>()

    val shopListItemSelectedAction = Action<Int>()
    val shopItemGeoPointSelectedAction = Action<GeoPoint>()
    val selectGeoPointCommand = Command<GeoPoint>()
    val selectShopItemCommand = Command<Int>()

    val searchItems = State<List<ListItem>>()
    val searchInput = inputControl()
    val searchClearAction = Action<Unit>()
    val searchCloseCommand = Command<Unit>()

    private val searchAction = Action<String>()
    private val searchResultSelectedAction = Action<SearchResultItem>()

    private val permissionStatusResultAction = Action<PermissionStatus>()
    private val fetchMyLocationAction = Action<Unit>()
    private val myLocationState = State<Location>()
    private val defaultLocationState = State<Location>()
    private val loadScreenAction = Action<Unit>()
    private val salePointsState = State<List<SalePoint>>()
    private val foundedLocation = State<Location>()
    private val selectedPointId = State<Any>()

    override fun onCreate() {
        super.onCreate()
        bindPermissionsBehaviour()
        bindLocationBehaviour()
        bindSalePoints()
        bindShopSelectionBehaviour()
        bindSearchBehaviour()
        bindClicks()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { Unit }
            .doOnNext(checkPermissionStatusCommand.consumer)
            .doOnNext(loadScreenAction.consumer)
            .subscribe()
            .untilDestroy()
    }

    fun setPermissionStatus(status: PermissionStatus) {
        permissionStatusResultAction.consumer.accept(status)
    }

    @SuppressLint("MissingPermission")
    private fun bindLocationBehaviour() {
        fetchMyLocationAction.observable
            .map { Unit }
            .flatMap {
                rxLocationManager.requestLocation()
                    .doOnNext(::handleLocationResult)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        moveToMyLocationAction.observable
            .map { myLocationState.valueOrNull ?: EMPTY_LOCATION }
            .filter { !it.isEmpty() }
            .doOnNext(showMyLocationCommand.consumer)
            .subscribe()
            .untilDestroy()

        myLocationState.observable
            .filter { !it.isEmpty() }
            .doOnNext(showMyLocationCommand.consumer)
            .doOnNext(foundedLocation.consumer)
            .subscribe()
            .untilDestroy()

        defaultLocationState.observable
            .doOnNext(showDefaultLocationCommand.consumer)
            .doOnNext(foundedLocation.consumer)
            .subscribe()
            .untilDestroy()
    }

    private fun bindPermissionsBehaviour() {
        permissionStatusResultAction.observable
            .subscribe { status ->
                when (status) {
                    PermissionStatus.REQUIRED -> requestPermissionCommand.consumer.accept(Unit)
                    PermissionStatus.GRANTED -> fetchMyLocationAction.consumer.accept(Unit)
                    else -> handleLocationResult(EMPTY_LOCATION)
                }
            }
            .untilDestroy()
    }

    private fun bindSalePoints() {
        loadScreenAction.observable
            .skipWhileInProgress()
            .flatMap { params ->
                getSalePointsUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnNext(salePointsState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observables.combineLatest(salePointsState.observable, foundedLocation.observable)
            .map {
                it.first.forEach { point ->
                    point.distance = it.second.distanceTo(point.coordinates)
                }
                it.first
            }
            .map { it.sortedBy { point -> point.distance } }
            .doOnNext(::displayPoints)
            .subscribe()
            .untilDestroy()
    }

    private fun displayPoints(points: List<SalePoint>) {
        items.consumer.accept(points.map { it.toItem() })
        geoPoints.consumer.accept(
            points.mapIndexed { index, point ->
                point.toGeoPoint().apply {
                    selected = if (!selectedPointId.hasValue()) {
                        index == 0
                    } else {
                        point.id == selectedPointId.value
                    }
                }
            }
        )
    }

    private fun bindShopSelectionBehaviour() {
        shopListItemSelectedAction.observable
            .filter { it.isInRange() }
            .map { items.valueOrNull?.get(it) }
            .map(::findGeoPointByShopItem)
            .filter { !it.isEmpty() }
            .doOnNext { selectedPointId.consumer.accept(it.id) }
            .doOnNext(selectGeoPointCommand.consumer)
            .subscribe()
            .untilDestroy()

        shopItemGeoPointSelectedAction.observable
            .map { it.id as String }
            .doOnNext { selectedPointId.consumer.accept(it) }
            .map(::findShopItemByGeoPoint)
            .filter { it != INVALID_INDEX }
            .doOnNext(selectShopItemCommand.consumer)
            .subscribe()
            .untilDestroy()

        searchResultSelectedAction.observable
            .map { it.id as String }
            .doOnNext { selectedPointId.consumer.accept(it) }
            .map(::findShopItemByGeoPoint)
            .filter { it != INVALID_INDEX }
            .doOnNext(selectShopItemCommand.consumer)
            .subscribe()
            .untilDestroy()
    }

    private fun bindSearchBehaviour() {
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
    }

    private fun bindClicks() {
        bus.clicks<Clicks>()
            .doOnNext(::processClick)
            .subscribe()
            .untilDestroy()
    }

    private fun processClick(clicks: Clicks) {
        when (clicks) {
            is Clicks.ShopMakeCall ->
                clicks.item.phone?.let { router.navigateTo(Screens.CallScreen(it)) }
            is Clicks.ShopMakeRoute ->
                findGeoPointByShopItem(clicks.item).let {
                    if (!it.isEmpty())
                        router.navigateTo(
                            Screens.NavigationScreen(
                                it.latitude,
                                it.longitude,
                                it.meta as String
                            )
                        )
                }
            is Clicks.SearchResult -> {
                searchInput.textChanges.consumer.accept("")
                searchCloseCommand.consumer.accept(Unit)
                searchItems.consumer.accept(emptyList())
                searchResultSelectedAction.consumer.accept(clicks.item)
            }
        }
    }

    private fun SalePoint.toItem(): ListItem =
        ShopItem(
            id = id,
            name = name,
            address = "$city, $address",
            distance = distance.formatDistance(resources),
            phone = phone
        )

    private fun SalePoint.toGeoPoint(): GeoPoint =
        GeoPoint(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            id = id,
            icon = type.toIcon(),
            meta = "$city, $address"
        )

    private fun Type.toIcon(): GeoPointIcon =
        when (this) {
            Type.SALE -> GeoPointIcon(normal = R.drawable.ic_normal_pin_shop, selected = R.drawable.ic_active_pin_shop)
            Type.SERVICE -> GeoPointIcon(normal = R.drawable.ic_normal_pin_services, selected = R.drawable.ic_active_pin_services)
        }

    private fun findGeoPointByShopItem(item: ListItem?): GeoPoint {
        (item as? ShopItem)?.let { shopItem ->
            return geoPoints.valueOrNull?.find { it.id == shopItem.id }?.also { it.selected = true }
                ?: emptyGeoPoint
        }
        return emptyGeoPoint
    }

    private fun findShopItemByGeoPoint(geoPointId: String): Int {
        return items.valueOrNull?.indexOfFirst { it is ShopItem && it.id == geoPointId }
            ?: INVALID_INDEX
    }

    private fun handleLocationResult(location: Location) {
        if (location.isEmpty()) defaultLocationState.consumer.accept(moskowLocation)
        else myLocationState.consumer.accept(location)
    }

    private fun Int.isInRange(): Boolean = this in 0 until items.value.size

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

    private fun SalePoint.toSearchItem(cardType: CardType): ListItem =
        SearchResultItem(
            id = id,
            name = name,
            address = "$city, $address",
            cardType = cardType
        )

    private companion object {
        const val INVALID_INDEX = -1
        const val INPUT_DELAY = 300L
    }
}