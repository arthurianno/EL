package com.elta.android.presentation.features.version.optional.ui

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import com.elta.android.domain.features.version.model.AppUpdateOffer
import com.elta.android.domain.features.version.model.AppUpdateStore
import com.elta.android.domain.features.version.repository.StoreUpdateRepository
import com.elta.android.presentation.R
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.elta.android.presentation.databinding.FragmentOptionalUpdateDialogBinding
import com.elta.android.presentation.features.version.openAppInStoreIntent
import com.elta.android.presentation.features.version.openAppUpdateOffer
import com.elta.android.presentation.features.version.optional.pm.OptionalUpdatePm
import com.jakewharton.rxbinding3.view.clicks
import me.dmdev.rxpm.bindTo
import javax.inject.Inject

class OptionalUpdateDialogFragment :
    BaseBottomSheetFragment<OptionalUpdatePm, FragmentOptionalUpdateDialogBinding>(
        FragmentOptionalUpdateDialogBinding::inflate
    ) {
    companion object {
        const val RESULT_KEY = "optional_update_dialog_result"
        const val RESULT_VISIBLE = "visible"
        private const val OFFERS_KEY = "update_offers"
        private const val PROMPT_RECORDED_KEY = "prompt_recorded"
        private const val PREVIEW_KEY = "debug_preview"

        fun newInstance(updates: List<AppUpdateOffer> = emptyList()) = OptionalUpdateDialogFragment().apply {
            arguments = Bundle().apply { putParcelableArrayList(OFFERS_KEY, ArrayList(updates)) }
        }

        fun newPreviewInstance() = newInstance(listOf(
            AppUpdateOffer(AppUpdateStore.GOOGLE_PLAY, 420,
                "https://play.google.com/store/apps/details?id=com.elta.android"),
            AppUpdateOffer(AppUpdateStore.RUSTORE, 420,
                "https://www.rustore.ru/catalog/app/com.elta.android")
        )).apply { checkNotNull(arguments).putBoolean(PREVIEW_KEY, true) }
    }

    @Inject
    lateinit var storeUpdates: StoreUpdateRepository

    private val updates: List<AppUpdateOffer>
        get() = arguments?.let {
            BundleCompat.getParcelableArrayList(it, OFFERS_KEY, AppUpdateOffer::class.java)
        }.orEmpty()
    private var promptRecorded = false
    private var storeChoiceDialog: AlertDialog? = null
    private val isPreview: Boolean
        get() = BuildConfig.DEBUG && arguments?.getBoolean(PREVIEW_KEY) == true

    override val screenLayout: Int = R.layout.fragment_optional_update_dialog
    override val classToken: Class<OptionalUpdatePm> = OptionalUpdatePm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        promptRecorded = savedInstanceState?.getBoolean(PROMPT_RECORDED_KEY) ?: false
    }

    override fun onStart() {
        super.onStart()
        if (!isPreview && !promptRecorded && updates.isNotEmpty()) {
            storeUpdates.recordPromptShown()
            promptRecorded = true
        }
        reportVisibility(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(PROMPT_RECORDED_KEY, promptRecorded)
        super.onSaveInstanceState(outState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        storeChoiceDialog?.dismiss()
        super.onDismiss(dialog)
        if (isAdded) reportVisibility(false)
    }

    override fun onDestroyView() {
        storeChoiceDialog?.dismiss()
        storeChoiceDialog = null
        super.onDestroyView()
    }

    override fun onBindPresentationModel(pm: OptionalUpdatePm) {
        binding.skipTextView.clicks().bindTo(pm.skipAction)
        binding.updateActionButtonView.clicks().bindTo(pm.mainAction)

        pm.closeDialogCommand.bindTo { dialog?.dismiss() }
        pm.openStoreCommand.bindTo {
            when (updates.size) {
                0 -> {
                    requireContext().openAppInStoreIntent(requireContext().packageName)
                    dismiss()
                }
                1 -> openUpdate(updates.first())
                else -> showStoreChoice()
            }
        }
    }

    private fun showStoreChoice() {
        if (storeChoiceDialog?.isShowing == true) return
        val offers = updates
        val names = offers.map { offer ->
            getString(when (offer.store) {
                AppUpdateStore.GOOGLE_PLAY -> R.string.update_store_google_play
                AppUpdateStore.RUSTORE -> R.string.update_store_rustore
            })
        }.toTypedArray()
        storeChoiceDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.update_choose_store)
            .setItems(names) { _, index -> openUpdate(offers[index]) }
            .setNegativeButton(android.R.string.cancel, null)
            .setOnDismissListener { storeChoiceDialog = null }
            .show()
    }

    private fun openUpdate(offer: AppUpdateOffer) {
        if (isPreview) {
            val name = getString(when (offer.store) {
                AppUpdateStore.GOOGLE_PLAY -> R.string.update_store_google_play
                AppUpdateStore.RUSTORE -> R.string.update_store_rustore
            })
            showToast(getString(R.string.update_preview_store_selected, name))
            return
        }
        if (requireContext().openAppUpdateOffer(offer)) dismiss()
        else showToast(getString(R.string.update_store_unavailable))
    }

    private fun reportVisibility(visible: Boolean) {
        parentFragmentManager.setFragmentResult(
            RESULT_KEY, Bundle().apply { putBoolean(RESULT_VISIBLE, visible) }
        )
    }
}
