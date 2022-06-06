package com.elta.android.presentation.features.profile.main.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.features.profile.main.pm.MainProfilePm
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.ui.DiabetesSettingDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.GlucoseRangeDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.HemoglobinSettingsFragment
import com.elta.android.presentation.utils.appbar.collapseProgress
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.ui.fragments.showDialog
import kotlinx.android.synthetic.main.fragment_main_profile.*
import me.dmdev.rxpm.bindTo
import kotlin.math.abs

class MainProfileFragment : BaseListFragment<MainProfilePm>() {

    override val screenLayout = R.layout.fragment_main_profile
    override val classToken: Class<MainProfilePm> = MainProfilePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentStatusBarConfigProvider
    override val backgroundColor = R.color.pale_gray

    override fun onBindPresentationModel(pm: MainProfilePm) {
        super.onBindPresentationModel(pm)
        observeAppBarChanges()
        pm.userFullNameState.bindTo(toolbarTitleView.text())
        pm.userFullNameState.bindTo(titleTextView.text())
        toolbarProfileSettingsButtonView.clicks().bindTo(pm.profileSettingsAction)
        bindProgressDialog(pm)
        pm.openDiabetesTypeDialogCommand.bindTo {
            childFragmentManager.showDialog(DiabetesSettingDialogFragment.newInstance())
        }
        pm.openHemoglobinTypeDialogCommand.bindTo {
            childFragmentManager.showDialog(HemoglobinSettingsFragment.newInstance())
        }
        pm.openGlucoseRangeDialogCommand.bindTo {
            childFragmentManager.showDialog(GlucoseRangeDialogFragment.newInstance())
        }
    }

    @Suppress("MagicNumber")
    private fun observeAppBarChanges() {
        appBarLayoutView.collapseProgress().subscribe {
            val alpha = 1 - abs(it / 100f)
            profileInfoView.alpha = if (alpha < 1) alpha - 0.7f else alpha
            toolbarProfileContainerView.alpha = 1 - alpha
        }
    }

    companion object {
        fun newInstance() = MainProfileFragment()
    }
}
