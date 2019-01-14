package com.nullgr.android.presentation.features.app.ui

import android.os.Bundle
import com.nullgr.android.presentation.R
import com.nullgr.android.presentation.core.ui.activity.BaseActivity
import com.nullgr.android.presentation.features.app.pm.AppPm

class AppActivity : BaseActivity<AppPm>() {

    override val screenLayout: Int = R.layout.layout_container
    override val classToken: Class<AppPm> = AppPm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            passTo(presentationModel.coldStartAction)
        }
    }
}