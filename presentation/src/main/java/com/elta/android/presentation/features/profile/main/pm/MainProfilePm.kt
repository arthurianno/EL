package com.elta.android.presentation.features.profile.main.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.main.ui.builder.MainProfileOptionsItemsBuilder
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class MainProfilePm @Inject constructor(
    private val itemsBuilder: MainProfileOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val createList = Command<Unit>()

    override fun onCreate() {
        super.onCreate()

        // todo hardcode for testing
        Timber.e("MainProfilePm  >> ")
        Observable.just(true)
            .doOnNext { Timber.e("MainProfilePm 1 >> $it") }
            .map { options -> itemsBuilder.buildItems() }
            .doOnNext(items.consumer)
            .doOnNext { Timber.e("MainProfilePm 2 >> $it") }
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()
    }
}