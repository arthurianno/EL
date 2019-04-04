package com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui

import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.pm.GlucoseRangeDialogPm
import com.nullgr.core.ui.extensions.toggleVisibilityState
import kotlinx.android.synthetic.main.fragment_base_settings_dialog.*
import kotlinx.android.synthetic.main.layout_settings_dialog_glucose.*
import timber.log.Timber

@Suppress("MagicNumber")
class GlucoseRangeDialogFragment : BaseSettingsDialogFragment<GlucoseRangeDialogPm>() {

    override val contentLayout = R.layout.layout_settings_dialog_glucose
    override val dialogType = DialogType.GLUCOSE
    override val classToken: Class<GlucoseRangeDialogPm> = GlucoseRangeDialogPm::class.java

    override fun onBindPresentationModel(pm: GlucoseRangeDialogPm) {
        super.onBindPresentationModel(pm)

        pm.progressState.bindTo {
            progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            glucoseRangeContentView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
        glucoseRangeBarView.setValues(3.9, 10.0)
        glucoseRangeBarView.valuesChanges().bindTo {
            Timber.d("onValues : {${it.first} / ${it.second}}")
        }
    }

    companion object {
        fun newInstance() = GlucoseRangeDialogFragment()
    }
}
