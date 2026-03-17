package com.elta.android.presentation.features.profile.main.ui

import android.util.Log
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentMainProfileBinding
import com.elta.android.presentation.features.profile.main.pm.MainProfilePm
import com.elta.android.presentation.features.profile.main.ui.adapter.MainProfileAdapter
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.ui.DiabetesSettingDialogFragment
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.HemoglobinSettingsFragment
import com.elta.android.presentation.utils.appbar.collapseProgress
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.ui.fragments.showDialog
import io.reactivex.rxkotlin.addTo
import me.dmdev.rxpm.bindTo
import javax.inject.Inject
import kotlin.math.abs

private const val NAV_TRACE_TAG = "NavTrace"

class MainProfileFragment :
    BaseRecyclerViewFragment<MainProfilePm, FragmentMainProfileBinding>(FragmentMainProfileBinding::inflate) {

    @Inject
    lateinit var mainProfileAdapter: MainProfileAdapter

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { mainProfileAdapter }
    override val screenLayout = R.layout.fragment_main_profile
    override val classToken: Class<MainProfilePm> = MainProfilePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentStatusBarConfigProvider

    override val backgroundColor = R.color.pale_gray

    override fun onResume() {
        super.onResume()
        Log.i(NAV_TRACE_TAG, "MainProfileFragment.onResume(parentBackStack=${parentFragmentManager.backStackEntryCount})")
    }

    override fun onBindPresentationModel(pm: MainProfilePm) {
        super.onBindPresentationModel(pm)
        observeAppBarChanges()
        pm.userFullNameState.bindTo(binding.toolbarTitleView.text())
        pm.userFullNameState.bindTo(binding.titleTextView.text())
        binding.toolbarProfileSettingsButtonView.clicks()
            .doOnNext { Log.i(NAV_TRACE_TAG, "MainProfileFragment UI click: profileSettingsButton") }
            .bindTo(pm.profileSettingsAction)
        bindProgressDialog(pm)
        pm.openDiabetesTypeDialogCommand.bindTo {
            childFragmentManager.showDialog(DiabetesSettingDialogFragment.newInstance())
        }
        pm.openHemoglobinTypeDialogCommand.bindTo {
            childFragmentManager.showDialog(HemoglobinSettingsFragment.newInstance())
        }
    }

    @Suppress("MagicNumber")
    private fun observeAppBarChanges() {
        binding.appBarLayoutView.collapseProgress().subscribe {
            val alpha = 1 - abs(it / 100f)
            binding.profileInfoView.alpha = if (alpha < 1) alpha - 0.7f else alpha
            binding.toolbarProfileContainerView.alpha = 1 - alpha
        }
            .addTo(compositeDestroy)
    }

    companion object {
        fun newInstance() = MainProfileFragment()
    }
}
