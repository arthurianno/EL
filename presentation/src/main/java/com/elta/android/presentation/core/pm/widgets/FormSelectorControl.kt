package com.elta.android.presentation.core.pm.widgets

import com.elta.android.presentation.widgets.selector.FormSelectorView
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.PresentationModel

@Suppress("UseDataClass")
class FormSelectorControl(pm: PresentationModel) {
    val option = pm.State(SelectorOption(null, null, null))
    val clickAction = pm.Action<Unit>()
}

fun PresentationModel.formSelectorControl(): FormSelectorControl = FormSelectorControl(this)

internal inline fun FormSelectorControl.bind(
    selectorView: FormSelectorView,
    compositeDisposable: CompositeDisposable
) {
    compositeDisposable.add(
        option.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(selectorView.value())
    )

    compositeDisposable.add(
        selectorView.click()
            .subscribe(clickAction.consumer)
    )
}