package com.elta.android.presentation.features.profile.settings.dialogs.base.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.CallSuper
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.databinding.FragmentBaseSettingsDialogBinding
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo

abstract class BaseSettingsDialogFragment<T : BaseSettingsDialogPm> :
    BaseBottomSheetFragment<T, FragmentBaseSettingsDialogBinding>(FragmentBaseSettingsDialogBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_base_settings_dialog
    protected abstract val contentLayout: Int
    protected abstract val dialogType: DialogType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            dialogIconView.setImageResource(dialogType.toIcon())
            dialogTitleView.setText(dialogType.toTitle())
            dialogSubTitleView.setText(dialogType.toSubTitle())
            dialogDescriptionTitleView.setText(dialogType.toDescription())
            dialogActionButtonView.setText(dialogType.toActionButtonTitle())
            LayoutInflater.from(activity).inflate(contentLayout, dialogContentContainerView, true)
        }
    }

    @CallSuper
    override fun onBindPresentationModel(pm: T) {
        binding.dialogCloseButtonView.clicks().subscribe { dialog?.dismiss() }
        binding.dialogActionButtonView.clicks().bindTo(pm.mainAction)
        pm.actionButtonEnabledCommand.bindTo(binding.dialogActionButtonView::setEnabled)
        pm.closeDialogCommand.bindTo { dialog?.dismiss() }
    }

    private fun DialogType.toIcon(): Int =
        when (this) {
            DialogType.DIABETES -> R.drawable.ic_diabet_big
            DialogType.GLUCOSE -> R.drawable.ic_range_big
            DialogType.HbA1C -> R.drawable.ic_hb_a_1_c_big
        }

    private fun DialogType.toTitle(): Int =
        when (this) {
            DialogType.DIABETES -> R.string.profile_settings_dialog_diabetes_title
            DialogType.GLUCOSE -> R.string.profile_settings_glucose_title
            DialogType.HbA1C -> R.string.profile_settings_dialog_hba1c_title
        }

    private fun DialogType.toSubTitle(): Int =
        when (this) {
            DialogType.DIABETES -> R.string.profile_settings_dialog_diabetes_subtitle
            DialogType.GLUCOSE -> R.string.profile_settings_glucose_subtitle
            DialogType.HbA1C -> R.string.profile_settings_dialog_hba1c_subtitle
        }

    private fun DialogType.toDescription(): Int =
        when (this) {
            DialogType.DIABETES -> R.string.profile_settings_dialog_diabetes_description
            DialogType.GLUCOSE -> R.string.profile_settings_glucose_description
            DialogType.HbA1C -> R.string.profile_settings_dialog_hba1c_description
        }

    private fun DialogType.toActionButtonTitle(): Int =
        when (this) {
            DialogType.DIABETES -> R.string.profile_settings_dialog_diabetes_action_button
            DialogType.GLUCOSE -> R.string.profile_settings_glucose_action_button
            DialogType.HbA1C -> R.string.profile_settings_dialog_hba1c_action_button
        }

    @Suppress("EnumNaming")
    protected enum class DialogType {
        DIABETES, GLUCOSE, HbA1C
    }
}
