package com.elta.android.presentation.features.profile.main.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.main.pm.MainProfilePm
import com.elta.android.presentation.utils.appbar.AppBarState
import com.elta.android.presentation.utils.appbar.collapseProgress
import com.elta.android.presentation.utils.appbar.observeState
import com.elta.android.presentation.utils.collapse
import com.elta.android.presentation.utils.expand
import kotlinx.android.synthetic.main.fragment_main_profile.*

class MainProfileFragment : BaseListFragment<MainProfilePm>() {

    override val screenLayout = R.layout.fragment_main_profile
    override val classToken: Class<MainProfilePm> = MainProfilePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override val backgroundColor = R.color.pale_gray

    override fun onBindPresentationModel(pm: MainProfilePm) {
        super.onBindPresentationModel(pm)
        observeAppBarChanges()
    }

    private fun observeAppBarChanges() {
        appBarLayoutView.observeState()
            .bindTo {
                toolbarUserNameTextView.apply {
                    if (it == AppBarState.COLLAPSED) toolbarUserNameTextView.expand()
                    else toolbarUserNameTextView.collapse()
                }
            }

        appBarLayoutView.collapseProgress().bindTo {
            val alpha = 1 - Math.abs(it / 100f)
            profileInfoView.alpha = alpha
        }
    }

    companion object {
        fun newInstance() = MainProfileFragment()
    }
}