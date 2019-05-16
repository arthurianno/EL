package com.elta.android.presentation.features.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.activity.BaseActivity
import com.elta.android.presentation.features.app.pm.AppPm
import com.elta.android.presentation.utils.dynamic_links.DynamicLinkProcessor
import kotlinx.android.synthetic.main.activity_app.*

class AppActivity : BaseActivity<AppPm>() {

    override val screenLayout: Int = R.layout.activity_app
    override val classToken: Class<AppPm> = AppPm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        DynamicLinkProcessor.from(intent)
            .ignoreColdStart(false)
            .withSavedState(savedInstanceState)
            .coldStartPassTo(presentationModel.coldStartAction)
            .deepLinkStartPassTo(presentationModel.deepLinkAction)
            .coldStartByDeepLinkPassTo(presentationModel.coldStartDeepLinkAction)
            .notificationStartPassTo(presentationModel.notificationStartAction)
            .build()
            .process()
    }

    override fun onBindPresentationModel(pm: AppPm) {
        super.onBindPresentationModel(pm)
        pm.networkStateCommand.bindTo(connectionStatusView.changeState())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        DynamicLinkProcessor.from(intent)
            .deepLinkStartPassTo(presentationModel.deepLinkAction)
            .notificationStartPassTo(presentationModel.notificationStartAction)
            .build()
            .process()
    }
}