package com.elta.android.presentation.features.main.records.pm

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
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
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.main.records.mapper.MainRecordsMapper
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import io.reactivex.Observable
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class MainRecordsPm @Inject constructor(
    private val getHomeModelUseCase: GetHomeModelUseCase,
    private val recordsMapper: MainRecordsMapper,
    services: ServiceFacade
) : BaseListPm(services) {

    val mainScreenState = stateControl()
    val googlePlayDialogControl = dialogControl<DialogData, DialogResult>()
    val feedbackDialogControl = dialogControl<DialogData, DialogResult>()

    private val loadScreenAction = Action<Unit>()
    private val googlePlayDialogAction = Action<Unit>()
    private val feedbackDialogAction = Action<Unit>()
    private val googlePlayDialogData by lazy { Dialogs.GooglePlayRateData(resources) }
    private val feedbackDialogData by lazy { Dialogs.FeedbackData(resources) }

    override fun onCreate() {
        super.onCreate()

        bindGooglePlayRateDialog()
        bindFeedbackDialog()

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

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bus.events<Events.EventsChanged>().map { Unit },
            bus.events<Events.ProfileUpdated>().map { Unit }
        )
            .doOnNext { loadScreenAction.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()
    }

    override fun onBind() {
        super.onBind()

        bus.clicks<Clicks.RecordClicked>()
            .map { it.item }
            .filter { it.eventType != EventType.GLUCOSE } // TODO
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
            isFirstEntrance -> {
                mainScreenState.dataState.consumer.accept(States.MainRecordsScreenFirstLaunchState(resources))
                mainScreenState.visibilityState.consumer.accept(true)
            }
            !hasEvents -> {
                mainScreenState.dataState.consumer.accept(States.MainRecordsScreenNewDayState(resources,
                    dayPeriod.greetingTitle()))
                mainScreenState.visibilityState.consumer.accept(true)
            }
            else -> mainScreenState.visibilityState.consumer.accept(false)
        }
    }

    private fun DayPeriod.greetingTitle(): Int =
        when (this) {
            DayPeriod.MORNING -> R.string.main_records_new_day_title_morning
            DayPeriod.AFTERNOON -> R.string.main_records_new_day_title_afternoon
            DayPeriod.EVENING -> R.string.main_records_new_day_title_evening
        }

    private fun bindGooglePlayRateDialog() =
        googlePlayDialogAction.observable
            .switchMapMaybe { googlePlayDialogControl.showForResult(googlePlayDialogData) }
            .subscribe()
            .untilDestroy()

    private fun bindFeedbackDialog() =
        feedbackDialogAction.observable
            .switchMapMaybe { feedbackDialogControl.showForResult(feedbackDialogData) }
            .subscribe()
            .untilDestroy()

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }
}