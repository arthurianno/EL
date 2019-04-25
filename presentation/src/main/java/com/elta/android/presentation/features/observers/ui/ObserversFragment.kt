package com.elta.android.presentation.features.observers.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.features.observers.pm.ObserversPm

class ObserversFragment : BaseListFragment<ObserversPm>() {

    override val screenLayout: Int = R.layout.fragment_observers
    override val classToken: Class<ObserversPm> = ObserversPm::class.java
    override val statusBarConfigProvider = LightStatusBarConfigProvider
    override val backgroundColor = R.color.pale_gray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onBindPresentationModel(pm: ObserversPm) {
        super.onBindPresentationModel(pm)
    }

    companion object {
        fun newInstance() = ObserversFragment()
    }
}