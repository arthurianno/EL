package com.elta.android.presentation.features.profile.settings.global.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentProfileSettingsBinding
import com.elta.android.presentation.features.googlefit.openGoogleFitInStoreIntent
import com.elta.android.presentation.features.googlefit.openGoogleFitIntent
import com.elta.android.presentation.features.profile.settings.global.pm.ProfileSettingsPm
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.ProfileSettingsAdapter
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.widgets.decoration.SettingsMarginItemDecoration
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.ui.fragments.showDialog
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo
import javax.inject.Inject

class ProfileSettingsFragment :
    BaseRecyclerViewFragment<ProfileSettingsPm, FragmentProfileSettingsBinding>(
        FragmentProfileSettingsBinding::inflate
    ) {
    companion object {
        fun newInstance() = ProfileSettingsFragment()
    }

    @Inject
    lateinit var profileSettingsAdapter: ProfileSettingsAdapter
    override val screenLayout: Int = R.layout.fragment_profile_settings
    override val classToken: Class<ProfileSettingsPm> = ProfileSettingsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { profileSettingsAdapter }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbarTitleView.text = getString(R.string.profile_settings)
        itemsView?.addItemDecoration(
            SettingsMarginItemDecoration(
                requireContext(),
                R.dimen.settings_top_margin,
                R.dimen.settings_bottom_margin
            )
        )
    }

    override fun onBindPresentationModel(pm: ProfileSettingsPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.unlinkNetworkDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.googleFitActivatedDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.openPrivacyPolicyCommand.bindTo {
            childFragmentManager.showDialog(
                RegistrationPrivacyPolicyFragment.newInstance(getString(R.string.registration_privacy_policy))
            )
        }
        pm.profileDeleteDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.copyTokenCommand.bindTo { copyToClipboard(it) }
        pm.downloadGoogleFitCommand.bindTo { requireContext().openGoogleFitInStoreIntent() }
        pm.openGoogleFitCommand.bindTo { requireContext().openGoogleFitIntent() }
        pm.emiasErrorDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    private fun copyToClipboard(message: String) {
        val clipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("TOKEN", message)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
