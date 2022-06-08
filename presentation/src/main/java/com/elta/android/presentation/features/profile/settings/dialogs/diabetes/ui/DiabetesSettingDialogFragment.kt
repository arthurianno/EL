package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
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

    private val diabetesViews = arrayListOf<TextView>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.firstColumnContainerView)
            .children().forEach { diabetesViews.add(it as TextView) }
        binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.secondColumnContainerView)
            .children().forEach { diabetesViews.add(it as TextView) }
    }

    override fun onBindPresentationModel(pm: DiabetesSettingDialogPm) {
        super.onBindPresentationModel(pm)
        pm.diabetesState.bindTo {
            it.forEachIndexed { index, diabetes ->
                diabetesViews[index].apply {
                    setText(diabetes.toStringRes())
                    tag = diabetes
                }
            }
        }
        pm.selectedDiabetesState.bindTo { selectedDiabetes ->
            diabetesViews.forEach {
                when {
                    it.isSelected -> it.isSelected = false
                    it.tag as Diabetes == selectedDiabetes -> it.isSelected = true
                }
            }
        }
        diabetesViews.forEach { view ->
            view.clicks()
                .subscribe { pm.diabetesTypeSelectedAction.consumer.accept(view.tag as Diabetes) }
        }
        pm.progressState.bindTo {
            binding.progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            binding.dialogContentContainerView.findViewById<LinearLayout>(R.id.diabetesContentView)
                .toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
    }

    companion object {
        fun newInstance() = DiabetesSettingDialogFragment()
    }
}
