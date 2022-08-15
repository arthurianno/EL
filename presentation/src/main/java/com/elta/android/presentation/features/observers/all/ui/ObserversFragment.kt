package com.elta.android.presentation.features.observers.all.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentObserversBinding
import com.elta.android.presentation.features.observers.all.pm.ObserversPm
import com.elta.android.presentation.features.observers.all.ui.adapter.ObserverAdapter
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.bindTo
import javax.inject.Inject

class ObserversFragment :
    BaseRecyclerViewFragment<ObserversPm, FragmentObserversBinding>(FragmentObserversBinding::inflate) {

    @Inject
    lateinit var obseverAdapter: ObserverAdapter

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { obseverAdapter }

    override val screenLayout: Int = R.layout.fragment_observers
    override val classToken: Class<ObserversPm> = ObserversPm::class.java

    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.menuButtonView.text = getString(R.string.profile_observers_invite)
    }

    override fun onBindPresentationModel(pm: ObserversPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.inviteObserverAction)
        pm.emptyControl.bind(binding.emptyStateView, compositeUnbind)
    }

    companion object {
        fun newInstance() = ObserversFragment()
    }
}
