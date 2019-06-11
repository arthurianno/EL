package com.elta.android.presentation.features.main.records.pm

import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.States
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.features.main.records.mapper.MainRecordsMapper
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import io.reactivex.Observable
import javax.inject.Inject

class MainRecordsPm @Inject constructor(
    private val getHomeModelUseCase: GetHomeModelUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
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
                    .flatMapSingle { homeModel ->
                        updateUserInfoUseCase
                            .execute(createUserInfoParams())
                            .toSingleDefault(homeModel)
                    }
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bus.events<Events.ProfileUpdated>().map { Unit }
        )
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()

        bus.events<Events.EventsChanged>()
            .doOnNext { loadScreenAction.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()
    }

    override fun onBind() {
        super.onBind()

        bus.clicks<Clicks.RecordClicked>()
            .map { it.item }
            .doOnNext(::navigateToEventScreen)
            .subscribe()
            .untilUnbind()
    }

    private fun navigateToEventScreen(record: RecordItem) {
        router.startFlow(Screens.EditEventScreen(record.id as String, record.eventType))
    }

    private fun handleSuccess(model: HomeModel) {
        bus.event(Events.HomeModelChanged(model))
        model.launchState()
        items.consumer.accept(recordsMapper.mapFromObject(model))
    }

    private fun HomeModel.launchState() {
        when {
            hasEvents -> mainScreenState.visibilityState.consumer.accept(false)
            isFirstEntrance -> {
                mainScreenState.dataState.consumer.accept(States.MainRecordsScreenFirstLaunchState(resources))
                mainScreenState.visibilityState.consumer.accept(true)
            }
            else -> {
                mainScreenState.dataState.consumer.accept(States.MainRecordsScreenNewDayState(resources,
                    dayPeriod.greetingTitle()))
                mainScreenState.visibilityState.consumer.accept(true)
            }
        }
    }

    private fun DayPeriod.greetingTitle(): Int =
        when (this) {
            DayPeriod.MORNING -> R.string.main_records_new_day_title_morning
            DayPeriod.AFTERNOON -> R.string.main_records_new_day_title_afternoon
            DayPeriod.EVENING -> R.string.main_records_new_day_title_evening
        }

    private fun createUserInfoParams(): UpdateUserInfoUseCase.Params =
        UpdateUserInfoUseCase.Params(UserInfo(isFirstHomeEntrance = false))
}