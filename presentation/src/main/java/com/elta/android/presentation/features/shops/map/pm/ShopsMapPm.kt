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
import com.elta.android.presentation.core.geo.ExtendedLocation
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
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.inputControl
import timber.log.Timber

@Suppress("TooManyFunctions")
class ShopsMapPm @Inject constructor(
    private val rxLocationManager: RxLocationManagerFixed,
    private val searchSalePointsUseCase: SearchSalePointsUseCase,
    private val getSalePointsUseCase: GetSalePointsUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val items = state<List<ListItem>>()
    val geoPoints = state<Pair<List<GeoPoint>, Int>>()
    val titleState = state<String>()
    val searchHintState = state<String>()

    val checkPermissionStatusCommand = command<Unit>(bufferSize = 1)
    val requestPermissionCommand = command<Unit>(bufferSize = 1)
    val showLocationPermissionDialog = command<Unit>(bufferSize = 1)
    val locationControl = locationControl(rxLocationManager)
    val openSettingsAction = action<Unit>()
    val openSettingsCommand = command<Unit>(bufferSize = 1)

    val addMyLocationPinCommand = command<Location>()
    val navigateToLocationCommand = command<ExtendedLocation>()
    val moveToMyLocationAction = action<Unit>()
    val skipAction = action<Unit>()

    val shopListItemSelectedAction = action<Int>()
    val shopItemGeoPointSelectedAction = action<GeoPoint>()
    val selectGeoPointCommand = command<GeoPoint>()
    val selectShopItemCommand = command<Int>()

    val searchItems = state<List<ListItem>>()
    val searchInput = inputControl()
    val searchClearAction = action<Unit>()
    val searchCloseCommand = command<Unit>()
    val showDefaultScreenStateCommand = command<List<Point>>()

    private val searchAction = action<String>()
    private val searchResultSelectedAction = action<SearchResultItem>()

    private val shopsTypeState = state<Type>()
    private val permissionStatusResultAction = action<PermissionStatus>()
    private val fetchMyLocationAction = action<Unit>()
    private val myLocationState = state<Location>()
    private val defaultLocationState = state<Location>()
    private val loadScreenAction = action<Unit>()
    private val salePointsState = state<List<SalePoint>>()
    private val foundedLocation = state<Location>()
    private val selectedPointId = state<Any>()
    private val coldStartState = state(true)
    private val manualNavigateToUserLocation = state(false)

    private val backgroundScheduler: Scheduler = Schedulers.computation()

    override fun onCreate() {
        super.onCreate()
        bindLocationBehaviour()
        bindSalePoints()
        bindShopSelectionBehaviour()
        bindSearchBehaviour()
        bindClicks()

        skipAction.observable
            .doOnNext(::navigateToMainScreen)
            .subscribe()
            .untilDestroy()

        openSettingsAction.observable
            .subscribe { openSettingsCommand.consumer.accept(Unit) }
            .untilDestroy()

        locationControl.locationEnabledAction.observable
            .subscribe(fetchMyLocationAction.consumer)
            .untilDestroy()

        locationControl.locationNotAllowedAction.observable
            .map { moskowLocation }
            .subscribe(defaultLocationState.consumer)
            .untilDestroy()

        shopsTypeState.observable
            .doOnNext { titleState.consumer.accept(it.toScreenTitle()) }
            .doOnNext { searchHintState.consumer.accept(it.toSearchHint()) }
            .subscribe()
            .untilDestroy()

        Observables.combineLatest(lifecycleObservable, shopsTypeState.observable)
            .filter { it.first == Lifecycle.CREATED }
            .map { Unit }
            .doOnNext(loadScreenAction.consumer)
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .checkAndRequestPermission()
            .subscribe { status ->
                when (status) {
                    PermissionStatus.GRANTED -> fetchMyLocationAction.consumer.accept(Unit)
                    PermissionStatus.DECLINED_NEVER_ASK -> showLocationPermissionDialog.consumer.accept(
                        Unit
                    )

                    else -> handleLocationResult(EMPTY_LOCATION)
                }
            }
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is LocationTurnedOffError -> locationControl.requestEnableLocationCommand.consumer.accept(
                Unit
            )

            is SecurityException -> Timber.i(error.message)
            else -> super.handleError(error)
        }
    }

    fun setShopsType(type: Type) {
        shopsTypeState.consumer.accept(type)
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
            .doOnNext { status ->
                when (status) {
                    PermissionStatus.GRANTED -> manualNavigateToUserLocation.consumer.accept(true)
                    PermissionStatus.DECLINED_NEVER_ASK -> showLocationPermissionDialog.consumer.accept(
                        Unit
                    )

                    else -> Unit
                }
            }
            .subscribe { fetchMyLocationAction.consumer.accept(Unit) }
            .untilDestroy()

        myLocationState.observable
            .filter { !it.isEmpty() }
            .doOnNext(foundedLocation.consumer)
            .doOnNext(addMyLocationPinCommand.consumer)
            .filter { manualNavigateToUserLocation.value }
            .doOnNext { navigateToLocationCommand.consumer.accept(ExtendedLocation(it)) }
            .doOnNext { manualNavigateToUserLocation.consumer.accept(false) }
            .subscribe()
            .untilDestroy()

        defaultLocationState.observable
            .doOnNext {
                navigateToLocationCommand.consumer.accept(
                    ExtendedLocation(
                        it,
                        DEFAULT_LOCATION_ZOOM
                    )
                )
            }
            .doOnNext(foundedLocation.consumer)
            .subscribe()
            .untilDestroy()
    }

    private fun bindSalePoints() {
        loadScreenAction.observable
            .skipWhileInProgress()
            .map(::createGetSalePointsParams)
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
            .observeOn(backgroundScheduler)
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

            else -> Unit
        }
    }

    private fun SalePoint.toItem(): ListItem =
        ShopItem(
            id = id,
            name = name,
            address = "$city, $address",
            distance = distance.formatDistance(resources),
            phone = phone,
            isSale = shopsTypeState.value == Type.SALE
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
            val geoPoint = geoPoints.valueOrNull?.first
                ?.find { it.id == shopItem.id }
                ?.apply {
                    meta = "${item.address}, ${item.name}"
                    selected = true
                } ?: emptyGeoPoint
            return geoPoint
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
        SearchSalePointsUseCase.Params(query, shopsTypeState.value)

    private fun createGetSalePointsParams(i: Unit) =
        GetSalePointsUseCase.Params(shopsTypeState.value)

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

    private fun Type.toScreenTitle(): String =
        when (this) {
            Type.SALE -> resources.getString(R.string.shops_map_toolbar_title)
            Type.SERVICE -> resources.getString(R.string.shops_map_toolbar_title_services)
        }

    private fun Type.toSearchHint(): String =
        when (this) {
            Type.SALE -> resources.getString(R.string.shops_search_hint)
            Type.SERVICE -> resources.getString(R.string.shops_search_hint_services)
        }

    private fun navigateToMainScreen(i: Unit) {
        router.newRootFlow(Screens.MainProfileScreen)
    }

    private companion object {
        const val INVALID_INDEX = -1
        const val INPUT_DELAY = 300L
        const val NEAREST_TEN_POINTS = 10
        const val DEFAULT_LOCATION_ZOOM = 10f
    }
}
