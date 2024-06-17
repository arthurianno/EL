package com.elta.android.presentation.features.devices.all.pm

import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingPathParam
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.devices.all.ui.builder.DevicesOptionsItemsBuilder
import com.nullgr.core.rx.bindEmpty
import io.reactivex.Observable
import me.dmdev.rxpm.action
import javax.inject.Inject

class DevicesPm @Inject constructor(
    private val getGlucometers: GetGlucometersUseCase,
    private val itemsBuilder: DevicesOptionsItemsBuilder,
    private val appMetric: AppMetricTracker,
    services: ServiceFacade
) : BaseListPm(services) {

    val addNewDeviceAction = action<Unit>()

    private val getGlucometersAction = action<Unit>()

    override fun onCreate() {
        super.onCreate()

        bindClicks()
        bindGlucometersAction()

        addNewDeviceAction.observable
            .doOnNext {
                appMetric.trackEvent(
                    AppMetricEvent.DeviceConnectingClick(ConnectingPathParam.MY_DEVICES)
                )
            }
            .subscribe { router.startFlow(Screens.ConnectTypeScreen(isOnBoarding = false)) }
            .untilDestroy()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { },
            bus.events<Events.DeviceChanged>().map { }
        )
            .subscribe(getGlucometersAction.consumer)
            .untilDestroy()
    }

    private fun bindClicks() {
        bus.clicks<Clicks.ActiveDeviceItemClicked>()
            .map { it.item }
            .map { Screens.DeviceInfo(it.name, it.address) }
            .subscribe(router::navigateTo)
            .untilDestroy()
    }

    private fun bindGlucometersAction() {
        getGlucometersAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getGlucometers.execute()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .map(itemsBuilder::buildItems)
                    .doOnSuccess(items.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }
}
