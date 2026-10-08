package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.core.ui.dialog.DialogResult
import io.reactivex.Completable
import io.reactivex.Single

internal enum class ManualGlucoseRoute {
    OPEN_ENTRY,
    SHOW_REMINDER
}

internal class HomeManualGlucosePrompt(
    private val shouldShowReminder: () -> Single<Boolean>,
    private val isEmiasLinked: () -> Single<Boolean>,
    private val disableReminder: () -> Completable
) {
    fun route(): Single<ManualGlucoseRoute> =
        shouldShowReminder().flatMap { showReminder ->
            if (!showReminder) {
                Single.just(ManualGlucoseRoute.OPEN_ENTRY)
            } else {
                isEmiasLinked()
                    .onErrorReturnItem(false)
                    .map { linked ->
                        if (linked) ManualGlucoseRoute.SHOW_REMINDER
                        else ManualGlucoseRoute.OPEN_ENTRY
                    }
            }
        }

    fun shouldOpenAfterDialog(result: DialogResult): Single<Boolean> =
        when (result) {
            DialogResult.POSITIVE -> Single.just(true)
            DialogResult.NEGATIVE -> Single.just(false)
            DialogResult.NEURAL -> disableReminder().andThen(Single.just(true))
        }
}
