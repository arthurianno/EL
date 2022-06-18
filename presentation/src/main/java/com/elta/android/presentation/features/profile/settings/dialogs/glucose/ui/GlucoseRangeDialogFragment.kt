package com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.pm.GlucoseRangeDialogPm
import com.elta.android.presentation.widgets.range_bar.RangeBarView
import com.nullgr.core.ui.extensions.toggleVisibilityState
import me.dmdev.rxpm.bindTo

class GlucoseRangeDialogFragment : BaseSettingsDialogFragment<GlucoseRangeDialogPm>() {

    override val contentLayout = R.layout.layout_settings_dialog_glucose
    override val dialogType = DialogType.GLUCOSE
    override val classToken: Class<GlucoseRangeDialogPm> = GlucoseRangeDialogPm::class.java

    private val glucoseRangeBarView by lazy {
        binding.dialogContentContainerView.findViewById<RangeBarView>(R.id.glucoseRangeBarView)
    }
    private val glucoseRangeContentView by lazy {
        binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.glucoseRangeContentView)
    }
    override fun onBindPresentationModel(pm: GlucoseRangeDialogPm) {
        super.onBindPresentationModel(pm)
        pm.glucoseRangeState.bindTo(glucoseRangeBarView.values())
        glucoseRangeBarView.valuesChanges().bindTo(pm.glucoseRangeChangedAction)
        pm.progressState.bindTo {
            binding.progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            glucoseRangeContentView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
    }

    companion object {
        fun newInstance() = GlucoseRangeDialogFragment()
    }
}
