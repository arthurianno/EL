package com.elta.android.presentation.features.observers.all.pm

import com.elta.android.domain.features.observers.interactor.DeleteObserverUseCase
import com.elta.android.domain.features.observers.interactor.GetObserverInvitesUseCase
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.observers.all.mapper.ObserverMapper
import com.elta.android.presentation.messages.SnackBarMessageData
import com.nullgr.core.rx.bindEmpty
import io.reactivex.Observable
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class ObserversPm @Inject constructor(
    private val mapper: ObserverMapper,
    private val getObserverInvitesUseCase: GetObserverInvitesUseCase,
    private val deleteObserverUseCase: DeleteObserverUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val deleteObserverDialogControl = dialogControl<DialogData, DialogResult>()
    val inviteObserverAction = Action<Unit>()

    private val getObserversAction = Action<Unit>()
    private val deleteObserverAction = Action<Unit>()
    private val selectedObserverState = State<String>()

    private val deleteObserverDialogData: DialogData by lazy { Dialogs.EventDeleteObserver(resources) }

    override fun onCreate() {
        super.onCreate()

        bus.clicks<Clicks.ObserverItemClicked>()
            .map { it.item.id }
            .doOnNext { selectedObserverState.consumer.accept(it) }
            .map { Unit }
            .subscribe(deleteObserverAction.consumer)
            .untilDestroy()

        deleteObserverAction.observable
            .skipWhileInProgress()
            .switchMapMaybe {
                deleteObserverDialogControl.showForResult(deleteObserverDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .map { selectedObserverState.value }
            .map(::createDeleteObserverParams)
            .flatMapCompletable { params ->
                deleteObserverUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleDeletingSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        inviteObserverAction.observable
            .doOnNext { router.navigateTo(Screens.InviteObserver) }
            .subscribe()
            .untilDestroy()

        getObserversAction.observable
            .flatMap {
                getObserverInvitesUseCase.execute()
                    .hideErrorContainer()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bus.events<Events.ObserverInvited>().map { Unit }
        )
            .subscribe(getObserversAction.consumer)
            .untilDestroy()
    }

    private fun handleSuccess(observers: List<Observer>) {
        items.consumer.accept(if (observers.isEmpty()) emptyList() else mapper.mapFromObject(observers))
    }

    private fun createDeleteObserverParams(id: String) = DeleteObserverUseCase.Params(id)

    private fun handleDeletingSuccess() {
        getObserversAction.consumer.accept(Unit)
        showSnackBar(
            SnackBarMessageData.SimpleTextMessage(
                resources.getString(R.string.profile_observers_delete_with_success)
            )
        )
    }

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }
}