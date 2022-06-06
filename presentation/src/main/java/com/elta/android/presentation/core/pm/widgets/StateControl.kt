@file:Suppress("NOTHING_TO_INLINE", "UseDataClass")

package com.elta.android.presentation.core.pm.widgets

import com.elta.android.presentation.core.ui.state_view.StateData
import com.elta.android.presentation.core.ui.state_view.StateView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state

class StateControl(pm: PresentationModel) {
    val dataState = pm.state<StateData>()
    val visibilityState = pm.state<Boolean>()
    val stateAction = pm.action<Unit>()
    val actionEnableState = pm.state(true)
}

fun PresentationModel.stateControl(): StateControl = StateControl(this)

@Suppress("LongMethod")
inline fun StateControl.bind(view: StateView, compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(
        dataState.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view.state())
    )

    compositeDisposable.add(
        visibilityState.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view.visibility())
    )

    compositeDisposable.add(
        view.clicks()
            .subscribe(stateAction.consumer)
    )

    compositeDisposable.add(
        actionEnableState.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view.enable())
    )
}
