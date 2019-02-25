package com.elta.android.presentation.features.main.records.pm

import android.graphics.drawable.Drawable
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.core.ui.state_view.StateData
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toIconWithBg
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.date.toDate
import com.nullgr.core.date.toStringWithFormat
import timber.log.Timber
import javax.inject.Inject

class MainRecordsPm @Inject constructor(
    private val getHomeModelUseCase: GetHomeModelUseCase,
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
        Timber.d(model.toString())

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

        items.consumer.accept(
            arrayListOf<ListItem>().apply {
                add(model.header())
                addAll(model.eventsBlocks.map { it.group() })
            }
        )
    }

    private fun EventsBlock.group(): ListItem =
        RecordsGroupItem(
            id = tag?.id ?: "tag",
            icon = tag.toIcon(),
            name = tag.toName(resources),
            items = events.map { it.record() }
        )

    private fun Event.record(): ListItem =
        RecordItem(
            id = id,
            icon = type.toIconWithBg(),
            title = this.toTitle(),
            type = resources.getString(type.toName()),
            count = formatValue(),
            date = formatDate(),
            showLabel = note != null
        )

    private fun Event.formatValue(): String? =
        when (type) {
            EventType.INSULIN -> resources.getString(R.string.event_type_insulin_pattern, checkNotNull(value))
            EventType.BREAD -> resources.getString(R.string.event_type_bread_pattern, checkNotNull(value))
            EventType.WEIGHT -> resources.getString(R.string.event_type_weight_pattern, checkNotNull(value))
            EventType.GLUCOSE -> resources.getString(R.string.event_type_glucose_pattern, checkNotNull(value))
            EventType.ACTIVITY -> checkNotNull(duration).toDate(CommonFormats.FORMAT_TIME_2)?.toStringWithFormat("HH ч mm мин ss сек")
            else -> null
        }

    private fun Event.formatDate(): String = additionTime.time.toString()

    private fun Event.toTitle(): String =
        when (type) {
            EventType.INSULIN -> resources.getString(checkNotNull(insulinType).toName())
            EventType.ACTIVITY -> activityType?.let { resources.getString(it.toName()) } ?: resources.getString(R.string.event_type_activity_no_name)
            EventType.BREAD -> kind?.let { it } ?: resources.getString(R.string.event_type_bread_no_name)
            EventType.MEDICAMENTS -> checkNotNull(name)
            EventType.WEIGHT -> resources.getString(R.string.weight_name)
            EventType.GLUCOSE -> resources.getString(R.string.event_type_glucose_no_name)
            else -> ""
        }

    private fun HomeModel.header(): ListItem =
        RecordsHeaderItem(
            background = glucoseLevel?.toBackground(),
            glucoseLevel = this.lastGlucoseEvent?.value,
            glucoseLevelIndex = this.glucoseLevelDifference,
            glucoseLevelIndexIcon = this.glucoseLevelDirection?.icon(),
            breadLevel = this.lastBreadEvent?.value,
            insulinLevel = this.lastInsulinEvent?.value
        )

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

    private fun GlucoseLevelDirection.icon(): Int? =
        when (this) {
            GlucoseLevelDirection.UP -> R.drawable.ic_change_index_up
            GlucoseLevelDirection.DOWN -> R.drawable.ic_change_index_down
            else -> null
        }

    private fun GlucoseLevel.toBackground(): Drawable? =
        when (this) {
            GlucoseLevel.HIGH -> resources.getDrawable(R.drawable.bg_gradient_red)
            GlucoseLevel.LOW -> resources.getDrawable(R.drawable.bg_gradient_blue)
            else -> resources.getDrawable(R.drawable.bg_gradient_green)
        }
}