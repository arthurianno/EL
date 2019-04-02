package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui

import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm.HemoglobinSettingsPm
import com.nullgr.core.ui.extensions.toggleVisibilityState
import kotlinx.android.synthetic.main.fragment_base_settings_dialog.*
import kotlinx.android.synthetic.main.layout_settings_dialog_diabetes.*

class HemoglobinSettingsFragment : BaseSettingsDialogFragment<HemoglobinSettingsPm>() {

    override val contentLayout = R.layout.layout_settings_dialog_hemoglobin
    override val dialogType = DialogType.HbA1C
    override val classToken: Class<HemoglobinSettingsPm> = HemoglobinSettingsPm::class.java

    override fun onBindPresentationModel(pm: HemoglobinSettingsPm) {
        super.onBindPresentationModel(pm)
        pm.progressState.bindTo {
            progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            diabetesContentView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
    }

    companion object {
        fun newInstance(): HemoglobinSettingsFragment = HemoglobinSettingsFragment()
    }
}
