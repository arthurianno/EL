package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.ui

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.extension.createDiabetesButtonView
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm.DiabetesSettingDialogPm
import com.elta.android.presentation.utils.toStringRes
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.children
import com.nullgr.core.ui.extensions.toggleVisibilityState
import me.dmdev.rxpm.bindTo

class DiabetesSettingDialogFragment : BaseSettingsDialogFragment<DiabetesSettingDialogPm>() {

    override val contentLayout = R.layout.layout_settings_dialog_diabetes
    override val dialogType = DialogType.DIABETES
    override val classToken: Class<DiabetesSettingDialogPm> = DiabetesSettingDialogPm::class.java

    private val contentView by lazy {
        binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.diabetesContentView)
    }

    override fun onBindPresentationModel(pm: DiabetesSettingDialogPm) {
        super.onBindPresentationModel(pm)
        pm.diabetesState.bindTo {
            it.forEach { diabetes ->
                val textView = createDiabetesButtonView(requireContext())
                    .apply {
                        setText(diabetes.toStringRes())
                        tag = diabetes
                        clicks().subscribe {
                            pm.diabetesTypeSelectedAction.consumer.accept(diabetes)
                        }
                    }
                contentView.addView(textView)
            }
        }
        pm.selectedDiabetesState.bindTo { selectedDiabetes ->
            contentView.children().forEach {
                if (it is TextView) {
                    when {
                        it.isSelected -> it.isSelected = false
                        it.tag as Diabetes == selectedDiabetes -> it.isSelected = true
                    }
                }
            }
        }
        pm.progressState.bindTo {
            binding.progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            contentView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
    }

    companion object {
        fun newInstance() = DiabetesSettingDialogFragment()
    }
}
