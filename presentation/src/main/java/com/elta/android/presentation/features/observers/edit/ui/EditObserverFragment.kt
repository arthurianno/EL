package com.elta.android.presentation.features.observers.edit.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentEditObserverBinding
import com.elta.android.presentation.features.observers.edit.pm.EditObserverPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.applyLengthFilter
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class EditObserverFragment :
    BaseFragment<EditObserverPm, FragmentEditObserverBinding>(FragmentEditObserverBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_edit_observer
    override val classToken: Class<EditObserverPm> = EditObserverPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(EXTRA_ID)?.let {
            presentationModel.setObserverId(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.menuButtonView.text =
            getString(R.string.profile_observer_edit_delete_button)
        binding.nameInputView.applyLengthFilter(MAX_NAME_LENGTH)
    }

    override fun onBindPresentationModel(pm: EditObserverPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.deleteObserverAction)
        binding.saveButtonView.clicks().bindTo(pm.saveObserverAction)
        pm.saveButtonEnabledState.bindTo { binding.saveButtonView.isEnabled = it }
        pm.observerNameInput.bindTo(binding.nameInputView)
        pm.deleteObserverDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    companion object {
        private const val EXTRA_ID = "extra_observer_id"
        private const val MAX_NAME_LENGTH = 100

        fun newInstance(id: String): EditObserverFragment {
            return EditObserverFragment().apply {
                arguments = bundle(EXTRA_ID to id)
            }
        }
    }
}
