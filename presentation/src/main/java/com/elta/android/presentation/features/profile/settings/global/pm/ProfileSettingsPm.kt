package com.elta.android.presentation.features.profile.settings.global.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.global.ui.builder.ProfileSettingsItemsBuilder
import timber.log.Timber
import javax.inject.Inject

class ProfileSettingsPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val itemsBuilder: ProfileSettingsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    private val buildItemsAction = Action<Unit>()

    override fun onBind() {
        super.onBind()
        observeClicks()

        // todo test
        buildItemsAction.observable
            .map { itemsBuilder.buildItems() }
            .doOnNext { Timber.e("BaseListPm >> $it") }
            .doOnNext(items.consumer)
            .doOnError(::handleError)
            .retry()
            .subscribe()
            .untilDestroy()

        // todo test
        lifecycleObservable
            .filter { it == Lifecycle.CREATED || it == Lifecycle.BINDED }
            .map { Unit }
            .subscribe(buildItemsAction.consumer)
            .untilDestroy()
    }

    private fun observeClicks() {
        bus.clicks<Clicks.ProfileSettingsItemClicked>()
            .map { it.type }
            .doOnNext {
                Timber.e("ProfileSettingsItemClicked >> ${it.name}")
            }
            .subscribe()
            .untilDestroy()
    }
}