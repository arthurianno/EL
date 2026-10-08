package com.elta.android.presentation.features.home.pm

import io.reactivex.Completable

/** Keeps the first-sync flag consistent when saving it fails. */
internal class HomeFirstSyncCompletion(
    private val saveCompleted: () -> Completable,
    private val setFirstSync: (Boolean) -> Unit,
    private val reportError: (Throwable) -> Unit
) {
    fun complete(): Completable = Completable.fromAction { setFirstSync(false) }
        .andThen(Completable.defer { saveCompleted() })
        .doOnError { error ->
            setFirstSync(true)
            reportError(error)
        }
        .onErrorComplete()
}
