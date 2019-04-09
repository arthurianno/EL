package com.elta.android.presentation.features.profile.settings.global.ui

import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.settings.global.pm.ProfileSettingsPm
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.widgets.SettingsMarginItemDecoration
import com.nullgr.core.ui.fragments.showDialog
import kotlinx.android.synthetic.main.layout_toolbar.*

class ProfileSettingsFragment : BaseListFragment<ProfileSettingsPm>() {

    override val screenLayout: Int = R.layout.fragment_profile_settings
    override val classToken: Class<ProfileSettingsPm> = ProfileSettingsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitleView.text = getString(R.string.profile_settings)
        itemsView?.addItemDecoration(
            SettingsMarginItemDecoration(
                checkNotNull(context),
                R.dimen.settings_top_margin,
                R.dimen.settings_bottom_margin
            )
        )
    }

    override fun onBindPresentationModel(pm: ProfileSettingsPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.unlinkNetworkDialogControl.bindTo { data, dc ->
            MaterialDialog.Builder(checkNotNull(activity))
                .cancelable(false)
                .title(data.title)
                .content(data.message)
                .negativeText(data.negative)
                .positiveText(data.positive)
                .onPositive { _, _ -> dc.sendResult(ProfileSettingsPm.DialogResult.POSITIVE) }
                .onNegative { _, _ -> dc.sendResult(ProfileSettingsPm.DialogResult.NEGATIVE) }
                .build()
        }
        pm.openPrivacyPolicyCommand.bindTo {
            childFragmentManager.showDialog(RegistrationPrivacyPolicyFragment.newInstance())
        }
    }

    companion object {
        fun newInstance() = ProfileSettingsFragment()
    }
}