@file:Suppress("NOTHING_TO_INLINE", "UseDataClass")

package com.elta.android.presentation.core.pm.widgets

import com.elta.android.presentation.core.ui.state_view.StateData
import com.elta.android.presentation.core.ui.state_view.StateView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.PresentationModel

class StateControl(pm: PresentationModel) {
    val dataState = pm.State<StateData>()
    val visibilityState = pm.State<Boolean>()
    val stateAction = pm.Action<Unit>()
    val actionEnableState = pm.State(true)
}

fun PresentationModel.stateControl(): StateControl = StateControl(this)

@Suppress("LongMethod")
inline fun StateControl.bind(view: StateView, compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(
        dataState.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view.state())
    )

    compositeDisposable.add(visibilityState.observable
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