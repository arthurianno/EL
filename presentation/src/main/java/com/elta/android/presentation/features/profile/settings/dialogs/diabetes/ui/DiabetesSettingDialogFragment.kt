package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.base.ui.BaseSettingsDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm.DiabetesSettingDialogPm
import com.elta.android.presentation.utils.toStringRes
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.children
import kotlinx.android.synthetic.main.fragment_base_settings_dialog.*
import kotlinx.android.synthetic.main.layout_diabetes_settings_dialog.*

class DiabetesSettingDialogFragment : BaseSettingsDialogFragment<DiabetesSettingDialogPm>() {

    override val contentLayout = R.layout.layout_diabetes_settings_dialog
    override val dialogType = DialogType.DIABETES
    override val classToken: Class<DiabetesSettingDialogPm> = DiabetesSettingDialogPm::class.java

    private val diabetesViews = arrayListOf<TextView>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstColumnContainerView.children().forEach { diabetesViews.add(it as TextView) }
        secondColumnContainerView.children().forEach { diabetesViews.add(it as TextView) }
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
        pm.selectedDiabetesState.bindTo { selected ->
            diabetesViews.find { it.isSelected }?.isSelected = false
            diabetesViews.find { it.tag as Diabetes == selected }?.isSelected = true
        }
        diabetesViews.forEach { view ->
            view.clicks().bindTo { pm.diabetesTypeSelectedAction.consumer.accept(view.tag as Diabetes) }
        }
        pm.actionButtonEnabledCommand.bindTo(dialogActionButtonView::setEnabled)
    }

    companion object {
        fun newInstance() = DiabetesSettingDialogFragment()
    }
}
