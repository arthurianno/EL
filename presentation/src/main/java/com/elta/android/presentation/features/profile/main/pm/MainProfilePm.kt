package com.elta.android.presentation.features.profile.main.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.main.ui.builder.MainProfileOptionsItemsBuilder
import io.reactivex.Single
import javax.inject.Inject

class MainProfilePm @Inject constructor(
    private val itemsBuilder: MainProfileOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val userFullNameState = State<String>()

    private val loadEventsAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        // todo hardcode for testing
        loadEventsAction.observable
            .skipWhileInProgress()
            .flatMapSingle { Single.just(itemsBuilder.buildItems()) }
            .doOnNext { setUpFullUserName() }
            .doOnNext(items.consumer)
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED || it == Lifecycle.BINDED }
            .map { Unit }
            .subscribe(loadEventsAction.consumer)
            .untilDestroy()
    }

    private fun setUpFullUserName() {
        userFullNameState.consumer.accept("Иванов Алексей")
    }
}