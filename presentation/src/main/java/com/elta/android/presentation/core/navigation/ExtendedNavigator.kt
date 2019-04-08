package com.elta.android.presentation.core.navigation

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.commands.AddTabs
import com.elta.android.presentation.core.navigation.commands.AttachTab
import com.nullgr.core.collections.isNotNullOrEmpty
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.android.support.SupportAppScreen
import ru.terrakok.cicerone.commands.Command

open class ExtendedNavigator(
    activity: FragmentActivity?,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : SupportAppNavigator(activity, fragmentManager, containerId) {

    private val tabsHolder = mutableMapOf<String, Fragment>()

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
            is AddTabs -> replaceTabFragments((command as AddTabs).screens)
            else -> super.applyCommand(command)
        }
    }

    private fun attachTabFragment(screen: SupportAppScreen) {
        checkScreenExistence(screen)
        fragmentManager.beginTransaction().apply {
            tabsHolder.forEach {
                when (screen.screenKey) {
                    it.key -> attach(it.value)
                    else -> detach(it.value)
                }
            }
        }.commitNow()
    }

    private fun replaceTabFragments(screens: Array<SupportAppScreen>) {
        tabsHolder.clear()
        screens.forEach { addTabFragment(it) }
    }

    private fun addTabFragment(screen: SupportAppScreen) {
        tabsHolder[screen.screenKey] = fragmentManager.initializeSingleTab(
            screen.fragment,
            containerId,
            screen.screenKey
        )
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

    private fun checkScreenExistence(screen: SupportAppScreen) {
        if (tabsHolder.isEmpty() && fragmentManager.fragments.isNotNullOrEmpty()) {
            fragmentManager.fragments.firstOrNull()?.let { fragment ->
                fragment.tag?.let {
                    tabsHolder[it] = fragment
                }
            }
        }
        if (!tabsHolder.containsKey(screen.screenKey)) {
            addTabFragment(screen)
        }
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
            "${command.javaClass.simpleName} command supports only fragments"
        )
    }
}