package com.elta.android.presentation.features.version.optional.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import javax.inject.Inject

class OptionalUpdatePm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val skipAction = action<Unit>()
    val mainAction = action<Unit>()

    val closeDialogCommand = command<Unit>()
    val openStoreCommand = command<Unit>()

    override fun onCreate() {
        super.onCreate()

        skipAction.observable
            .subscribe(closeDialogCommand.consumer)
            .untilDestroy()

        mainAction.observable
            .subscribe(openStoreCommand.consumer)
            .untilDestroy()
    }
}
