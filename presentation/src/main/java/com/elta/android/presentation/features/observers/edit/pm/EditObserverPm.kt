package com.elta.android.presentation.features.observers.edit.pm

import com.elta.android.domain.features.observers.interactor.DeleteObserverUseCase
import com.elta.android.domain.features.observers.interactor.GetObserverUseCase
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.messages.SnackBarMessageData
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EditObserverPm @Inject constructor(
    private val deleteObserverUseCase: DeleteObserverUseCase,
    private val getObserverUseCase: GetObserverUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val observerNameInput = inputControl()
    val saveButtonEnabledState = State(false)
    val deleteObserverDialogControl = dialogControl<DialogData, DialogResult>()
    val deleteObserverAction = Action<Unit>()
    val saveObserverAction = Action<Unit>()

    private val observerDeletedSuccessAction = Action<Unit>()
    private val selectedObserverIdState = State<String>()
    private val observerState = State<Observer>()

    private val deleteObserverDialogData: DialogData by lazy { Dialogs.EventDeleteObserver(resources) }

    fun setObserverId(id: String) {
        selectedObserverIdState.consumer.accept(id)
    }

    override fun onCreate() {
        super.onCreate()
        bindDeleteBehaviour()
        bindObserverState()

        Observables.combineLatest(
            lifecycleObservable,
            selectedObserverIdState.observable
        )
            .filter { it.first == Lifecycle.CREATED }
            .map { it.second }
            .map(::createGetObserverParams)
            .flatMapSingle {
                getObserverUseCase.execute(it)
                    .bindProgress()
                    .doOnSuccess(observerState.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindObserverState() {
        observerState.observable
            .filter { it.name != null }
            .map { checkNotNull(it.name) }
            .doOnNext(observerNameInput.text.consumer)
            .subscribe()
            .untilDestroy()

        observerNameInput.textChanges.observable
            .map { !it.isNotEmpty() && it != observerState.value.name }
            .doOnNext(saveButtonEnabledState.consumer)
            .subscribe()
            .untilDestroy()
    }

    private fun bindDeleteBehaviour() {
        deleteObserverAction.observable
            .skipWhileInProgress()
            .switchMapMaybe {
                deleteObserverDialogControl.showForResult(deleteObserverDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .map { selectedObserverIdState.value }
            .map(::createDeleteObserverParams)
            .flatMapCompletable { params ->
                deleteObserverUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete { observerDeletedSuccessAction.consumer.accept(Unit) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        observerDeletedSuccessAction.observable
            .doOnNext {
                showSnackBar(
                    SnackBarMessageData.SimpleTextMessage(
                        resources.getString(R.string.profile_observers_delete_with_success)
                    )
                )
            }
            .delay(AFTER_DELETE_DELAY, TimeUnit.MILLISECONDS)
            .doOnNext {
                bus.event(Events.ObserversUpdated)
                router.exit()
            }
            .subscribe()
            .untilDestroy()
    }

    private fun createDeleteObserverParams(id: String) = DeleteObserverUseCase.Params(id)

    private fun createGetObserverParams(id: String) = GetObserverUseCase.Params(id)

    companion object {
        private const val AFTER_DELETE_DELAY = 1000L
    }
}