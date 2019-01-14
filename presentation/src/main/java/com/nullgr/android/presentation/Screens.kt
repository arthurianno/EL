package com.nullgr.android.presentation

import android.support.v4.app.Fragment
import com.nullgr.android.presentation.features.onboaring.ui.OnBoardingFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

object Screens {

    object OnBoardingFlow : SupportAppScreen() {
        override fun getFragment(): Fragment = OnBoardingFragment.newInstance()
    }
}