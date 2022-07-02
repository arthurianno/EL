package com.elta.android.presentation.features.profile.support.ui

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentSupportBinding
import com.elta.android.presentation.features.profile.support.pm.SupportPm
import com.elta.android.presentation.features.profile.support.ui.adapter.SupportAdapter
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class SupportFragment :
    BaseRecyclerViewFragment<SupportPm, FragmentSupportBinding>(FragmentSupportBinding::inflate) {

    @Inject
    lateinit var supportAdapter: SupportAdapter
    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { supportAdapter }
    override val screenLayout: Int = R.layout.fragment_support
    override val classToken: Class<SupportPm> = SupportPm::class.java

    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    companion object {
        fun newInstance() = SupportFragment()
    }
}
