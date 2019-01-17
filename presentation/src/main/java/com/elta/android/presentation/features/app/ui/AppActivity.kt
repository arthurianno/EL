package com.elta.android.presentation.features.app.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.activity.BaseActivity
import com.elta.android.presentation.features.app.pm.AppPm
import kotlinx.android.synthetic.main.activity_app.*

class AppActivity : BaseActivity<AppPm>() {

    override val screenLayout: Int = R.layout.activity_app
    override val classToken: Class<AppPm> = AppPm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            passTo(presentationModel.coldStartAction)
        }
    }

    override fun onBindPresentationModel(pm: AppPm) {
        super.onBindPresentationModel(pm)
        pm.networkStateCommand.bindTo(connectionStatusView.connectionState())
    }
}