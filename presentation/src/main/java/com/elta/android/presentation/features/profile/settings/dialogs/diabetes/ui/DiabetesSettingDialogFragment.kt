package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm.DiabetesSettingDialogPm

class DiabetesSettingDialogFragment : BaseSettingsDialogFragment<DiabetesSettingDialogPm>() {

    override val contentLayout = R.layout.layout_diabetes_settings_dialog
    override val dialogType = DialogType.DIABETES
    override val classToken: Class<DiabetesSettingDialogPm> = DiabetesSettingDialogPm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onBindPresentationModel(pm: DiabetesSettingDialogPm) {
        super.onBindPresentationModel(pm)
    }

    companion object {
        fun newInstance() = DiabetesSettingDialogFragment()
    }
}
