package com.elta.android.presentation.features.profile.settings.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.ui.builder.ProfileSettingsItemsBuilder
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ProfileSettingsPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val itemsBuilder: ProfileSettingsItemsBuilder,
    private val resourceProvider: ResourceProvider,
    services: ServiceFacade
) : BaseListPm(services) {

    override fun onBind() {
        super.onBind()
    }
}