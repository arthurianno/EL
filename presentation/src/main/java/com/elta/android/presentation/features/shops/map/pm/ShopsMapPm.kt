package com.elta.android.presentation.features.shops.map.pm

import android.annotation.SuppressLint
import android.location.Location
import com.elta.android.domain.features.sale_points.interactor.GetSalePointsUseCase
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.presentation.core.geo.GeoPoint
import com.elta.android.presentation.core.permissions.PermissionStatus
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.elta.android.presentation.utils.distanceTo
import com.elta.android.presentation.utils.formatDistance
import com.elta.android.presentation.utils.moskowLocation
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.location.EMPTY_LOCATION
import com.nullgr.core.rx.location.RxLocationManager
import com.nullgr.core.rx.location.isEmpty
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class ShopsMapPm @Inject constructor(
    private val rxLocationManager: RxLocationManager,
    private val getSalePointsUseCase: GetSalePointsUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val items = State<List<ListItem>>()
    val geoPoints = State<List<GeoPoint>>()

    val permissionStatusUpdatedAction = Action<PermissionStatus>()
    val permissionRequiredCommand = Command<Unit>()
    val showMyLocationCommand = Command<Location>()
    val showDefaultLocationCommand = Command<Location>()
    val fetchMyLocationAction = Action<Unit>()

    private val myLocationState = State<Location>()
    private val defaultLocationState = State<Location>()
    private val permissionStatusState = State<PermissionStatus>()
    private val loadScreenAction = Action<Unit>()
    private val salePointsState = State<List<SalePoint>>()
    private val foundedLocation = State<Location>()

    override fun onCreate() {
        super.onCreate()
        bindPermissionsBehaviour()
        bindLocationBehaviour()
        bindSalePoints()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()
    }

    private fun bindLocationBehaviour() {
        fetchMyLocationAction.observable
            .filter { permissionStatusState.valueOrNull == PermissionStatus.GRANTED }
            .map { Unit }
            .doOnNext(::fetchMyLocation)
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
        loadScreenAction.observable
            .flatMap { permissionStatusUpdatedAction.observable }
            .subscribe(permissionStatusState.consumer)
            .untilDestroy()

        permissionStatusState.observable
            .doOnNext {
                when (it) {
                    PermissionStatus.REQUIRED -> permissionRequiredCommand.consumer.accept(Unit)
                    PermissionStatus.GRANTED -> fetchMyLocationAction.consumer.accept(Unit)
                    else -> handleLocationResult(EMPTY_LOCATION)
                }
            }
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

        // TODO have an issue when user taps on my location and it scrolls to selected shop
        // TODO Need to be improved
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
        geoPoints.consumer.accept(points.mapIndexed { index, point ->
            val geoPoint = point.toGeoPoint()
            // TODO this behaviour should be changed
            geoPoint.selected = index == 0
            geoPoint
        })
    }

    private fun SalePoint.toItem(): ListItem =
        ShopItem(
            id = id,
            name = name,
            address = fullAddress,
            distance = distance.formatDistance(resources)
        )

    private fun SalePoint.toGeoPoint(): GeoPoint =
        GeoPoint(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            id = id
        )

    private fun handleLocationResult(location: Location) {
        if (location.isEmpty()) defaultLocationState.consumer.accept(moskowLocation)
        else myLocationState.consumer.accept(location)
    }

    @SuppressLint("MissingPermission")
    private fun fetchMyLocation(i: Unit) {
        rxLocationManager.requestLocation()
            .doOnNext(::handleLocationResult)
            .subscribe()
            .untilDestroy()
    }
}