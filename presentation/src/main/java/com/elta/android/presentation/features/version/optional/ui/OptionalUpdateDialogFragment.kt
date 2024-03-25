package com.elta.android.presentation.features.version.optional.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.databinding.FragmentOptionalUpdateDialogBinding
import com.elta.android.presentation.features.version.openAppInStoreIntent
import com.elta.android.presentation.features.version.optional.pm.OptionalUpdatePm
import com.jakewharton.rxbinding3.view.clicks
import me.dmdev.rxpm.bindTo

class OptionalUpdateDialogFragment :
    BaseBottomSheetFragment<OptionalUpdatePm, FragmentOptionalUpdateDialogBinding>(
        FragmentOptionalUpdateDialogBinding::inflate
    ) {
    companion object {
        fun newInstance() = OptionalUpdateDialogFragment()
    }

    override val screenLayout: Int = R.layout.fragment_optional_update_dialog
    override val classToken: Class<OptionalUpdatePm> = OptionalUpdatePm::class.java

    override fun onBindPresentationModel(pm: OptionalUpdatePm) {
        binding.skipTextView.clicks().bindTo(pm.skipAction)
        binding.updateActionButtonView.clicks().bindTo(pm.mainAction)

        pm.closeDialogCommand.bindTo { dialog?.dismiss() }
        pm.openStoreCommand.bindTo {
            requireContext().openAppInStoreIntent(requireContext().packageName)
        }
    }

}
