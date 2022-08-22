package com.elta.android.presentation.features.main.events.create.pm

import com.elta.android.common.utils.isDateChanged
import com.elta.android.domain.features.diary.events.interactor.AddNewEventUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.model.EventFormModel
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.state
import javax.inject.Inject

class EventCreationPm @Inject constructor(
    private val addNewEventUseCase: AddNewEventUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    services: ServiceFacade,
) : BaseEventPm(services) {

    private val isFormNotEmptyState = state(false)
    private val eventFormHolderState = state(EventFormModel())

    override fun onCreate() {
        super.onCreate()

        getProfileAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getProfileUseCase.execute()
                    .bindProgress()
                    .hideErrorContainer()
                    .doOnSuccess(profileState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(getProfileAction.consumer)
            .untilDestroy()

        mainActionTitleState.consumer.accept(resources.getString(R.string.event_form_save_new_entry_title))
        observeSaveEventAction()
    }

    override fun handleBack(i: Unit) {
        when (isFormNotEmptyState.value) {
            true -> exitDialogAction.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    override fun observeEventChanges() {
        Observables.combineLatest(
            eventTypeState.observable,
            formPickerValue.observable,
            formInput.text.observable,
            formSelector.option.observable,
            tagSelector.option.observable,
            selectedDateState.observable,
            noteInput.text.observable
        ) { eventType, pickerValue, inputValue, variant, tag, date, note ->
            eventFormHolderState.value.apply {
                this.eventType = eventType
                this.pickerValue = pickerValue
                this.inputValue = inputValue
                this.tag = tag.meta as? Tag
                this.isDateChanged = this.date.isDateChanged(date)
                this.date = date
                this.noteValue = note
                this.meta = variant.meta
            }
        }
            .doOnNext(::checkIsEmpty)
            .map(::isFormValid)
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()
    }

    private fun checkIsEmpty(eventFormModel: EventFormModel) {
        if (eventTypeState.valueOrNull == EventType.WEIGHT) {
            isFormNotEmptyState.consumer.accept(
                eventFormModel.pickerValue != (profileState.valueOrNull?.weight ?: 0.0) ||
                    !eventFormModel.inputValue.isNullOrEmpty() ||
                    eventFormModel.meta != null ||
                    eventFormModel.tag != null ||
                    !eventFormModel.note.isNullOrEmpty() ||
                    eventFormModel.isDateChanged
            )
        } else {
            isFormNotEmptyState.consumer.accept(
                eventFormModel.pickerValue != ZERO_PICKER_VALUE ||
                    !eventFormModel.inputValue.isNullOrEmpty() ||
                    eventFormModel.meta != null ||
                    eventFormModel.tag != null ||
                    !eventFormModel.note.isNullOrEmpty() ||
                    eventFormModel.isDateChanged
            )
        }
    }

    private fun observeSaveEventAction() {
        mainAction.observable
            .skipWhileInProgress()
            .map(::createAddEventParams)
            .flatMapSingle { params ->
                addNewEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .trackEvent { createCreationEvent(params) }
                    .doOnComplete { sendEventIfNeed(params) }
                    .andThen(Single.just(true))
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun createAddEventParams(i: Unit): AddNewEventUseCase.Params {
        val form = eventFormHolderState.value
        return AddNewEventUseCase.Params(
            value = form.value,
            kind = form.kind,
            name = form.name,
            duration = form.duration,
            date = form.date,
            tag = form.tag,
            activity = form.activityType,
            insulin = form.insulin?.type,
            medicament = form.insulin?.drug,
            note = form.note,
            eventType = checkNotNull(form.eventType)
        )
    }

    private fun createCreationEvent(params: AddNewEventUseCase.Params): AnalyticsEvent? {
        val data = hashMapOf<String, String>()
        val name = when (params.eventType) {
            EventType.BREAD -> AnalyticsEventType.EVENT_BREAD_ADD
            EventType.WEIGHT -> AnalyticsEventType.EVENT_WEIGHT_ADD
            EventType.MEDICAMENTS -> AnalyticsEventType.EVENT_MEDICAMENTS_ADD
            EventType.ACTIVITY -> {
                params.activity?.let { data[AnalyticsEventParam.TYPE] = it.name }
                AnalyticsEventType.EVENT_ACTIVITY_ADD
            }
            EventType.INSULIN -> {
                data[AnalyticsEventParam.TYPE] = checkNotNull(params.insulin).name
                AnalyticsEventType.EVENT_INSULIN_ADD
            }
            else -> null
        }
        return if (name == null) null else AnalyticsEvent(name, data)
    }

    private fun sendEventIfNeed(params: AddNewEventUseCase.Params) {
        if (params.eventType == EventType.WEIGHT) bus.event(Events.ShouldUpdateProfile)
    }

    companion object {
        private const val ZERO_PICKER_VALUE = 0.0
    }
}
