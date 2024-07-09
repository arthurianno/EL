package com.elta.android.presentation.features.sync.start.base.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentSyncStartBinding
import com.elta.android.presentation.features.sync.start.base.pm.SyncStartPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import me.dmdev.rxpm.bindTo

abstract class SyncStartFragment<T : SyncStartPm> :
    BaseFragment<T, FragmentSyncStartBinding>(FragmentSyncStartBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_sync_start
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.hide()
            // todo: SalepointHide
            // скрываем точки продаж пока пока не примем решение что с ними делать
//            menuButtonView.text = getString(R.string.sync_start_menu_button_text)
                // Когда вернём карты, то убрать эту заглушку
            menuButtonView.hide()
        }
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.skipAction)
        binding.actionButtonView.clicks().bindTo(pm.mainAction)
    }
}
