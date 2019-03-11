package com.elta.android.presentation.features.main.events.edit.pm

import com.elta.android.domain.features.diary.events.interactor.GetEventByIdUseCase
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.features.main.events.edit.pm.binder.getFormInputText
import com.elta.android.presentation.features.main.events.edit.pm.binder.getPickerValues
import com.elta.android.presentation.features.main.events.edit.pm.binder.getSelectorOption
import com.elta.android.presentation.features.main.events.edit.pm.binder.getTag
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class EditEventPm @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    services: ServiceFacade
) : BaseEventPm(services) {

    val deleteEventAction = Action<Unit>()

    private val eventId = State<String>()
    private val event = State<Event>()
    private val loadScreenAction = Action<String>()

    fun setEventId(id: String) {
        eventId.consumer.accept(id)
    }

    override fun onCreate() {
        super.onCreate()
        observeEventChanges()

        loadScreenAction.observable
            .map(::createUseCaseParams)
            .flatMapSingle {
                getEventByIdUseCase.execute(it)
                    .bindProgress()
                    .doOnSuccess(event.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        event.observable
            .doOnNext(::bindEvent)
            .subscribe()
            .untilDestroy()

        Observables.combineLatest(
            lifecycleObservable,
            eventId.observable
        )
            .filter { it.first == Lifecycle.CREATED }
            .map { it.second }
            .doOnNext { loadScreenAction.consumer.accept(it) }
            .subscribe()
            .untilDestroy()
    }

    private fun bindEvent(event: Event) {
        event.getPickerValues()?.let { updateFormPickerValueCommand.consumer.accept(it) }
        event.getFormInputText()?.let { formInput.text.consumer.accept(it) }
        event.getSelectorOption(resources)?.let { formSelector.option.consumer.accept(it) }
        event.getTag()?.let { tagSelector.option.consumer.accept(it) }
        dateTimeSelectedAction.consumer.accept(event.additionTime)
        event.note?.let { noteInput.text.consumer.accept(it) }
    }

    override fun handleBack(i: Unit) {
        router.exit()
    }

    private fun observeEventChanges() {

    }

    private fun createUseCaseParams(id: String) =
        GetEventByIdUseCase.Params(id)
}