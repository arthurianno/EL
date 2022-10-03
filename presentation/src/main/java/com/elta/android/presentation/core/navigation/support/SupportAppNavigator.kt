package com.elta.android.presentation.core.navigation.support

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.BackTo
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.Forward
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.Replace

/**
 * Navigator implementation for launch fragments and activities.<br></br>
 * Feature [BackTo] works only for fragments.<br></br>
 * Recommendation: most useful for Single-Activity application.
 */
private const val MARKET_SCHEME = "market"
private const val MARKET_AUTHORITY = "details"
private const val MARKET_ID = "id"

open class SupportAppNavigator(
    private val activity: FragmentActivity,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : Navigator {
    private val localStackCopy = mutableListOf<String>()

    override fun applyCommands(commands: Array<out Command>) {
        fragmentManager.executePendingTransactions()
        copyStackToLocal()
        commands.forEach { applyCommand(it) }
    }

    private fun copyStackToLocal() {
        localStackCopy.clear()
        val stackSize: Int = fragmentManager.backStackEntryCount
        for (i in 0 until stackSize) {
            localStackCopy.add(fragmentManager.getBackStackEntryAt(i).name.orEmpty())
        }
    }

    protected fun applyCommand(command: Command) {
        when (command) {
            is Forward -> activityForward(command)
            is Replace -> activityReplace(command)
            is BackTo -> backTo(command)
            is Back -> fragmentBack()
        }
    }

    protected fun activityForward(command: Forward) {
        val screen = command.screen as SupportAppScreen
        screen.getActivityIntent(activity)?.let { activityIntent ->
            val options: Bundle? = createStartActivityOptions(command, activityIntent)
            checkAndStartActivity(screen, activityIntent, options)
        }
            ?: fragmentForward(command)
    }

    protected fun fragmentForward(command: Forward) {
        val screen = command.screen as SupportAppScreen
        val fragment: Fragment = createFragment(screen)
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        setupFragmentTransaction(
            command,
            fragmentManager.findFragmentById(containerId),
            fragment,
            fragmentTransaction
        )
        fragmentTransaction
            .replace(containerId, fragment)
            .addToBackStack(screen.screenKey)
            .commit()
        localStackCopy.add(screen.screenKey)
    }

    protected fun fragmentBack() {
        if (localStackCopy.isNotEmpty()) {
            fragmentManager.popBackStack()
            localStackCopy.removeLast()
        } else {
            activityBack()
        }
    }

    protected fun activityBack() {
        activity.finish()
    }

    protected fun activityReplace(command: Replace) {
        val screen = command.screen as SupportAppScreen
        screen.getActivityIntent(activity)?.let { activityIntent ->
            val options: Bundle? = createStartActivityOptions(command, activityIntent)
            checkAndStartActivity(screen, activityIntent, options)
            activity.finish()
        }
            ?: fragmentReplace(command)
    }

    protected fun fragmentReplace(command: Replace) {
        val screen = command.screen as SupportAppScreen
        val fragment: Fragment = createFragment(screen)
        if (localStackCopy.isNotEmpty()) {
            fragmentManager.popBackStack()
            localStackCopy.removeLast()
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            setupFragmentTransaction(
                command,
                fragmentManager.findFragmentById(containerId),
                fragment,
                fragmentTransaction
            )
            fragmentTransaction
                .replace(containerId, fragment)
                .addToBackStack(screen.screenKey)
                .commit()
            localStackCopy.add(screen.screenKey)
        } else {
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            setupFragmentTransaction(
                command,
                fragmentManager.findFragmentById(containerId),
                fragment,
                fragmentTransaction
            )
            fragmentTransaction
                .replace(containerId, fragment)
                .commit()
        }
    }

    protected fun backTo(command: BackTo) {
        if (command.screen == null) {
            backToRoot()
        } else {
            val key: String = command.screen?.screenKey.orEmpty()
            val index = localStackCopy.indexOf(key)
            val size = localStackCopy.size
            if (index != -1) {
                for (i in 1 until size - index) {
                    localStackCopy.removeLast()
                }
                fragmentManager.popBackStack(key, 0)
            } else {
                backToUnExisting(command.screen as SupportAppScreen)
            }
        }
    }

    private fun backToRoot() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        localStackCopy.clear()
    }

    /**
     * Override this method to setup fragment transaction [FragmentTransaction].
     * For example: setCustomAnimations(...), addSharedElement(...) or setReorderingAllowed(...)
     *
     * @param command             current navigation command. Will be only [Forward] or [Replace]
     * @param currentFragment     current fragment in container
     * (for [Replace] command it will be screen previous in new chain, NOT replaced screen)
     * @param nextFragment        next screen fragment
     * @param fragmentTransaction fragment transaction
     */
    open fun setupFragmentTransaction(
        command: Command?,
        currentFragment: Fragment?,
        nextFragment: Fragment?,
        fragmentTransaction: FragmentTransaction
    ) {
    }

    protected fun createStartActivityOptions(command: Command?, activityIntent: Intent?): Bundle? {
        return null
    }

    protected fun checkAndStartActivity(
        screen: SupportAppScreen,
        activityIntent: Intent?,
        options: Bundle?
    ) {
        runCatching {
            activity.startActivity(activityIntent, options)
        }.onFailure {
            if (it is ActivityNotFoundException) {
                unExistingActivity(screen, activityIntent)
            }
        }
    }

    protected fun unExistingActivity(screen: SupportAppScreen?, activityIntent: Intent?) {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.Builder()
                    .scheme(MARKET_SCHEME)
                    .authority(MARKET_AUTHORITY)
                    .appendQueryParameter(MARKET_ID, activityIntent?.`package`)
                    .build()
            )
        )
    }

    protected fun createFragment(screen: SupportAppScreen): Fragment =
        screen.getFragment() ?: throw RuntimeException("Can't create a screen: " + screen.screenKey)

    protected fun backToUnExisting(screen: SupportAppScreen?) {
        backToRoot()
    }
}
