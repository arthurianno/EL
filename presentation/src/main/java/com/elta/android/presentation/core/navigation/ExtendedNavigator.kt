package com.elta.android.presentation.core.navigation

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.commands.AddTabs
import com.elta.android.presentation.core.navigation.commands.AttachTab
import ru.terrakok.cicerone.Screen
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.android.support.SupportAppScreen
import ru.terrakok.cicerone.commands.Command

open class ExtendedNavigator(
    activity: FragmentActivity?,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : SupportAppNavigator(activity, fragmentManager, containerId) {

    private val tabsHolder = mutableMapOf<Screen, Fragment>()

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

    override fun applyCommand(command: Command) {
        when (checkCondition(command)) {
            is AttachTab -> attachTabFragment((command as AttachTab).screen)
            is AddTabs -> addTabFragments((command as AddTabs).screens)
            else -> super.applyCommand(command)
        }
    }

    private fun attachTabFragment(screen: SupportAppScreen) {
        fragmentManager.beginTransaction().apply {
            tabsHolder.forEach {
                when (screen) {
                    it.key -> attach(it.value)
                    else -> detach(it.value)
                }
            }
        }.commitNow()
    }

    private fun addTabFragments(screens: Array<SupportAppScreen>) {
        tabsHolder.clear()
        screens.forEach {
            tabsHolder[it] = fragmentManager.initializeSingleTab(
                it.fragment,
                containerId,
                it.screenKey
            )
        }
    }

    private fun FragmentManager.initializeSingleTab(
        fragment: Fragment,
        containerId: Int,
        tag: String
    ): Fragment =
        findFragmentByTag(tag) ?: fragment.apply {
            beginTransaction()
                .add(containerId, this, tag)
                .detach(this)
                .commitNow()
        }

    private fun checkCondition(command: Command): Command {
        when (command) {
            is AttachTab ->
                if (command.screen.fragment == null) throwInvalidConditionException(command)
            is AddTabs ->
                command.screens.forEach {
                    if (it.fragment == null) throwInvalidConditionException(command)
                }
        }
        return command
    }

    private fun throwInvalidConditionException(command: Command) {
        throw IllegalStateException(
            "${command.javaClass.simpleName} connectAction supports only fragments"
        )
    }
}