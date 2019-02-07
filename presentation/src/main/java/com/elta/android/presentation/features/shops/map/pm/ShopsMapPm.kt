package com.elta.android.presentation.features.shops.map.pm

import android.annotation.SuppressLint
import android.location.Location
import com.elta.android.presentation.R
import com.elta.android.presentation.core.permissions.PermissionStatus
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.location.EMPTY_LOCATION
import com.nullgr.core.rx.location.RxLocationManager
import com.nullgr.core.rx.location.isEmpty
import javax.inject.Inject

class ShopsMapPm @Inject constructor(
    private val rxLocationManager: RxLocationManager,
    services: ServiceFacade
) : BasePm(services) {

    val items = State<List<ListItem>>()

    val permissionStatusUpdatedAction = Action<PermissionStatus>()
    val permissionRequiredCommand = Command<Unit>()
    val showMyLocationCommand = Command<Location>()
    val fetchMyLocationAction = Action<Unit>()

    private val myLocationState = State<Location>()
    private val permissionStatusState = State<PermissionStatus>()
    private val loadScreenAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()
        bindPermissionsBehaviour()
        bindLocationBehaviour()
        bindShopsItems()
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
                    else -> myLocationState.consumer.accept(EMPTY_LOCATION)
                }
            }
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()
    }

    private fun bindShopsItems() {
        items.consumer.accept(
            mutableListOf<ListItem>().apply {
                repeat((0..10).count()) {
                    add(
                        ShopItem(
                            id = it,
                            name = "Test Name #$it",
                            address = "Test Address #$it",
                            distance = resources.getString(R.string.shops_map_distance_km_pattern, it)
                        )
                    )
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchMyLocation(i: Unit) {
        rxLocationManager.requestLocation()
            .doOnNext(myLocationState.consumer)
            .subscribe()
            .untilDestroy()
    }
}