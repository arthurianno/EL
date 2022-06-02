package com.elta.android.presentation.features.observers.all.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.observers.all.pm.ObserversPm
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.layout_toolbar.*
import me.dmdev.rxpm.bindTo

class ObserversFragment : BaseListFragment<ObserversPm>() {

    override val screenLayout: Int = R.layout.fragment_observers
    override val classToken: Class<ObserversPm> = ObserversPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.text = getString(R.string.profile_observers_invite)
    }

    override fun onBindPresentationModel(pm: ObserversPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        menuButtonView.clicks().bindTo(pm.inviteObserverAction)
    }

    companion object {
        fun newInstance() = ObserversFragment()
    }
}
