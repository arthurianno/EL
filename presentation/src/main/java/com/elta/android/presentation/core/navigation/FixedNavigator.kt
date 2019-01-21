package com.elta.android.presentation.core.navigation

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import com.elta.android.presentation.R
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command

open class FixedNavigator(
    activity: FragmentActivity
) : SupportAppNavigator(activity, activity.supportFragmentManager, R.id.containerView) {

    override fun setupFragmentTransaction(
        command: Command?,
        currentFragment: Fragment?,
        nextFragment: Fragment?,
        fragmentTransaction: FragmentTransaction
    ) {
        fragmentTransaction.apply {
            setReorderingAllowed(true)
            setCustomAnimations(R.animator.fade_in_animator, 0, 0, 0)
        }
    }
}