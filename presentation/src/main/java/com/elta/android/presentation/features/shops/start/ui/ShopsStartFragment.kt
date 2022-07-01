package com.elta.android.presentation.features.shops.start.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentShopsStartBinding
import com.elta.android.presentation.features.shops.start.pm.ShopsStartPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import me.dmdev.rxpm.bindTo

class ShopsStartFragment :
    BaseFragment<ShopsStartPm, FragmentShopsStartBinding>(FragmentShopsStartBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_shops_start
    override val classToken: Class<ShopsStartPm> = ShopsStartPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.hide()
            menuButtonView.text = getString(R.string.shops_start_menu_button_text)
        }
    }

    override fun onBindPresentationModel(pm: ShopsStartPm) {
        super.onBindPresentationModel(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.skipAction)
        binding.findShopsButtonView.clicks().bindTo(pm.findShopAction)
    }

    companion object {
        fun newInstance() = ShopsStartFragment()
    }
}
