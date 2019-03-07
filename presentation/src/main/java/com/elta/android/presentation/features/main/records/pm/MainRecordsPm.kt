package com.elta.android.presentation.features.main.records.pm

import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.States
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.core.ui.state_view.StateData
import com.elta.android.presentation.features.main.records.MainRecordsMapper
import javax.inject.Inject

class MainRecordsPm @Inject constructor(
    private val getHomeModelUseCase: GetHomeModelUseCase,
    private val recordsMapper: MainRecordsMapper,
    services: ServiceFacade
) : BaseListPm(services) {

    val mainScreenState = stateControl()

    private val loadScreenAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        loadScreenAction.observable
            .skipWhileInProgress()
            .flatMap { params ->
                getHomeModelUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .doOnNext { loadScreenAction.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()
    }

    private fun handleSuccess(model: HomeModel) {
        bus.event(Events.HomeModelChanged(model))

        if (model.isFirstEntrance) {
            mainScreenState.dataState.consumer.accept(model.launchState())
            mainScreenState.visibilityState.consumer.accept(true)
        } else if (!model.hasEvents) {
            mainScreenState.dataState.consumer.accept(model.launchState())
            mainScreenState.visibilityState.consumer.accept(true)
        } else {
            mainScreenState.visibilityState.consumer.accept(false)
        }

        items.consumer.accept(recordsMapper.mapFromObject(model))
        router.navigateTo(Screens.ShopsStart)
    }

    private fun HomeModel.launchState(): StateData? =
        when {
            this.isFirstEntrance -> States.MainRecordsScreenFirstLaunchState(resources)
            !this.hasEvents -> States.MainRecordsScreenNewDayState(resources, dayPeriod.greetingTitle())
            else -> null
        }

    private fun DayPeriod.greetingTitle(): Int =
        when (this) {
            DayPeriod.MORNING -> R.string.main_records_new_day_title_morning
            DayPeriod.AFTERNOON -> R.string.main_records_new_day_title_afternoon
            DayPeriod.EVENING -> R.string.main_records_new_day_title_evening
        }
}