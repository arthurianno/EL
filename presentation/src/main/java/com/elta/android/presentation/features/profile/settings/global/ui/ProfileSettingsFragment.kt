package com.elta.android.presentation.features.profile.settings.global.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentProfileSettingsBinding
import com.elta.android.presentation.features.profile.settings.global.pm.ProfileSettingsPm
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.widgets.decoration.SettingsMarginItemDecoration
import com.nullgr.core.ui.fragments.showDialog
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class ProfileSettingsFragment : BaseListFragment<ProfileSettingsPm, FragmentProfileSettingsBinding>(
    FragmentProfileSettingsBinding::inflate
) {

    override val screenLayout: Int = R.layout.fragment_profile_settings
    override val classToken: Class<ProfileSettingsPm> = ProfileSettingsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

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
    }

    companion object {
        fun newInstance() = ProfileSettingsFragment()
    }
}
