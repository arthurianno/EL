package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.elta.android.domain.features.user.model.GlucoseFormat

data class OnBoardingGlucoseFormatItem(
    override val title: String
) : OnBoardingItem {
    override val data: Any
        get() = format

    var format: GlucoseFormat = GlucoseFormat.CAPILLARY
}
