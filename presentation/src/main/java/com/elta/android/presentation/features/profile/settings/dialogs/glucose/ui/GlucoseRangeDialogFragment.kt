package com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui

import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.pm.GlucoseRangeDialogPm
import com.elta.android.presentation.widgets.rangebar.RangeBarView
import com.nullgr.core.ui.extensions.toggleVisibilityState
import me.dmdev.rxpm.bindTo

class GlucoseRangeDialogFragment : BaseSettingsDialogFragment<GlucoseRangeDialogPm>() {

    override val contentLayout = R.layout.layout_settings_dialog_glucose
    override val dialogType = DialogType.GLUCOSE
    override val classToken: Class<GlucoseRangeDialogPm> = GlucoseRangeDialogPm::class.java

    private val beforeEatGlucoseRangeBarView by lazy {
        binding.dialogContentContainerView.getGlucoseRangeBar(
            cardView = R.id.beforeEatDiapason,
            title = R.string.before_eat
        )
    }
    private val afterEatGlucoseRangeBarView by lazy {
        binding.dialogContentContainerView.getGlucoseRangeBar(
            cardView = R.id.afterEatDiapason,
            title = R.string.after_eat
        )
    }
    private val glucoseRangeContentView by lazy {
        binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.glucoseRangeContentView)
    }

    override fun onBindPresentationModel(pm: GlucoseRangeDialogPm) {
        super.onBindPresentationModel(pm)
        pm.beforeEatGlucoseRangeState.bindTo(beforeEatGlucoseRangeBarView.values())
        pm.afterEatGlucoseRangeState.bindTo(afterEatGlucoseRangeBarView.values())
        beforeEatGlucoseRangeBarView.valuesChanges().bindTo(pm.beforeEatGlucoseRangeChangedAction)
        afterEatGlucoseRangeBarView.valuesChanges().bindTo(pm.afterEatGlucoseRangeChangedAction)
        pm.progressState.bindTo {
            binding.progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            glucoseRangeContentView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
    }

    companion object {
        fun newInstance() = GlucoseRangeDialogFragment()
    }

    private fun FrameLayout.getGlucoseRangeBar(
        @IdRes cardView: Int,
        @StringRes title: Int
    ): RangeBarView =
        this.findViewById<CardView>(cardView)
            .apply {
                findViewById<AppCompatTextView>(R.id.glucoseDiapasonTitle).setText(title)
            }
            .findViewById(R.id.glucoseRangeBarView)
}
