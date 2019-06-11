@file:Suppress("TooManyFunctions")

package com.elta.android.presentation.features.shops.map.pm

import android.annotation.SuppressLint
import android.location.Location
import com.elta.android.common.utils.takeFirst
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
import com.elta.android.presentation.core.geo.LocationTurnedOffError
import com.elta.android.presentation.core.geo.RxLocationManagerFixed
import com.elta.android.presentation.core.geo.emptyGeoPoint
import com.elta.android.presentation.core.geo.isEmpty
import com.elta.android.presentation.core.permissions.PermissionStatus
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.locationControl
import com.elta.android.presentation.core.ui.adapter.CardType
import com.elta.android.presentation.core.ui.adapter.getCardType
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchHeaderItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.elta.android.presentation.utils.distanceTo
import com.elta.android.presentation.utils.formatDistance
import com.elta.android.presentation.utils.moskowLocation
import com.elta.android.presentation.utils.toPoint
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.location.EMPTY_LOCATION
import com.nullgr.core.rx.location.isEmpty
import com.yandex.mapkit.geometry.Point
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.inputControl
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Suppress("TooManyFunctions")
class ShopsMapPm @Inject constructor(
    private val rxLocationManager: RxLocationManagerFixed,
    private val searchSalePointsUseCase: SearchSalePointsUseCase,
    private val getSalePointsUseCase: GetSalePointsUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val items = State<List<ListItem>>()
    val geoPoints = State<Pair<List<GeoPoint>, Int>>()

    val checkPermissionStatusCommand = Command<Unit>(bufferSize = 1)
    val requestPermissionCommand = Command<Unit>(bufferSize = 1)
    val locationControl = locationControl(rxLocationManager)

    val addMyLocationPinCommand = Command<Location>()
    val navigateToLocationCommand = Command<Location>()
    val moveToMyLocationAction = Action<Unit>()

    val shopListItemSelectedAction = Action<Int>()
    val shopItemGeoPointSelectedAction = Action<GeoPoint>()
    val selectGeoPointCommand = Command<GeoPoint>()
    val selectShopItemCommand = Command<Int>()

    val searchItems = State<List<ListItem>>()
    val searchInput = inputControl()
    val searchClearAction = Action<Unit>()
    val searchCloseCommand = Command<Unit>()
    val showDefaultScreenStateCommand = Command<List<Point>>()

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
    private val coldStartState = State(true)
    private val manualNavigateToUserLocation = State(false)

    override fun onCreate() {
        super.onCreate()
        bindLocationBehaviour()
        bindSalePoints()
        bindShopSelectionBehaviour()
        bindSearchBehaviour()
        bindClicks()

        locationControl.locationEnabledAction.observable
            .subscribe(fetchMyLocationAction.consumer)
            .untilDestroy()

        locationControl.locationNotAllowedAction.observable
            .map { moskowLocation }
            .subscribe(defaultLocationState.consumer)
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { Unit }
            .doOnNext(loadScreenAction.consumer)
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .checkAndRequestPermission()
            .subscribe { status ->
                if (status == PermissionStatus.GRANTED) fetchMyLocationAction.consumer.accept(Unit)
                else handleLocationResult(EMPTY_LOCATION)
            }
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        if (error is LocationTurnedOffError) locationControl.requestEnableLocationCommand.consumer.accept(Unit)
        else super.handleError(error)
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
            .checkAndRequestPermission()
            .filter { it == PermissionStatus.GRANTED }
            .map { Unit }
            .doOnNext { manualNavigateToUserLocation.consumer.accept(true) }
            .subscribe(fetchMyLocationAction.consumer)
            .untilDestroy()

        myLocationState.observable
            .filter { !it.isEmpty() }
            .doOnNext(foundedLocation.consumer)
            .doOnNext(addMyLocationPinCommand.consumer)
            .filter { manualNavigateToUserLocation.value }
            .doOnNext(navigateToLocationCommand.consumer)
            .doOnNext { manualNavigateToUserLocation.consumer.accept(false) }
            .subscribe()
            .untilDestroy()

        defaultLocationState.observable
            .doOnNext(navigateToLocationCommand.consumer)
            .doOnNext(foundedLocation.consumer)
            .subscribe()
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
                val points = it.first
                val location = it.second

                if (!location.isEmpty() && location != defaultLocationState.valueOrNull) {
                    points.forEach { point ->
                        point.distance = location.distanceTo(point.coordinates)
                    }
                }
                points
            }
            .map { it.sortedBy { point -> point.distance } }
            .doOnNext(::displayPoints)
            .filter { coldStartState.value && foundedLocation.value != defaultLocationState.valueOrNull }
            .map { buildColdStartPoints() }
            .doOnNext(showDefaultScreenStateCommand.consumer)
            .doOnNext { coldStartState.consumer.accept(false) }
            .subscribe()
            .untilDestroy()
    }

    private fun displayPoints(points: List<SalePoint>) {
        items.consumer.accept(points.map { it.toItem() })
        var selectedIndex = 0
        val mappedPoints = points.mapIndexed { index, point ->
            point.toGeoPoint().apply {
                selected = if (!selectedPointId.hasValue()) {
                    index == 0
                } else {
                    point.id == selectedPointId.value
                }
                if (selected) selectedIndex = index
            }
        }
        geoPoints.consumer.accept(mappedPoints to selectedIndex)
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
            .map(::createSearchParams)
            .switchMap { params ->
                searchSalePointsUseCase.execute(params)
                    .hideErrorContainer()
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

    private inline fun SalePoint.toItem(): ListItem =
        ShopItem(
            id = id,
            name = name,
            address = "$city, $address",
            distance = distance.formatDistance(resources),
            phone = phone
        )

    private inline fun SalePoint.toGeoPoint(): GeoPoint =
        GeoPoint(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            id = id,
            icon = type.toIcon(),
            meta = "$city, $address"
        )

    private inline fun Type.toIcon(): GeoPointIcon =
        when (this) {
            Type.SALE -> GeoPointIcon(
                normal = R.drawable.ic_normal_pin_shop,
                selected = R.drawable.ic_active_pin_shop
            )
            Type.SERVICE -> GeoPointIcon(
                normal = R.drawable.ic_normal_pin_services,
                selected = R.drawable.ic_active_pin_services
            )
        }

    private fun findGeoPointByShopItem(item: ListItem?): GeoPoint {
        (item as? ShopItem)?.let { shopItem ->
            return geoPoints.valueOrNull?.first?.find { it.id == shopItem.id }?.also { it.selected = true }
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

    private fun <T> Observable<T>.checkAndRequestPermission(): Observable<PermissionStatus> =
        this.doOnNext { checkPermissionStatusCommand.consumer.accept(Unit) }
            .switchMap {
                permissionStatusResultAction.observable
                    .take(1)
                    .switchMap { status ->
                        if (status == PermissionStatus.REQUIRED)
                            permissionStatusResultAction.observable
                                .take(1)
                                .doOnSubscribe {
                                    requestPermissionCommand.consumer.accept(Unit)
                                }
                        else Observable.just(status)
                    }
            }

    private fun buildColdStartPoints(): List<Point> =
        arrayListOf<Point>().apply {
            add(foundedLocation.value.toPoint())
            addAll(geoPoints.value.first.takeFirst(NEAREST_TEN_POINTS).map { it.toPoint() })
        }

    private companion object {
        const val INVALID_INDEX = -1
        const val INPUT_DELAY = 300L
        const val NEAREST_TEN_POINTS = 10
    }
}