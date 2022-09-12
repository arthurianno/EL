package com.elta.android.presentation.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.commands.AddTabs
import com.elta.android.presentation.core.navigation.commands.AttachTab
import com.elta.android.presentation.core.navigation.support.SupportAppNavigator
import com.elta.android.presentation.core.navigation.support.SupportAppScreen
import com.github.terrakok.cicerone.Command
import com.nullgr.core.collections.isNotNullOrEmpty

open class ExtendedNavigator(
    activity: FragmentActivity,
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

    override fun applyCommands(commands: Array<out Command>) {
        commands.forEach { command ->
            when (checkCondition(command)) {
                is AttachTab -> attachTabFragment((command as AttachTab).screen)
                is AddTabs -> replaceTabFragments((command as AddTabs).screens)
                else -> super.applyCommand(command)
            }
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
        screen.getFragment()?.let {
            tabsHolder[screen.screenKey] = fragmentManager.initializeSingleTab(
                it,
                containerId,
                screen.screenKey
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
                if (command.screen.getFragment() == null) throwInvalidConditionException(command)
            is AddTabs ->
                command.screens.forEach {
                    if (it.getFragment() == null) throwInvalidConditionException(command)
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
