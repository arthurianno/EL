package com.elta.android.presentation.core.navigation.support

import android.app.Activity
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
    activity: FragmentActivity,
    fragmentManager: FragmentManager,
    containerId: Int
) : Navigator {
    private val activity: Activity
    private val fragmentManager: FragmentManager
    private val containerId: Int
    private val localStackCopy = mutableListOf<String>()

    override fun applyCommands(commands: Array<out Command>) {
        fragmentManager.executePendingTransactions()

        // copy stack before apply commands
        copyStackToLocal()
        for (command in commands) {
            applyCommand(command)
        }
    }

    private fun copyStackToLocal() {
        localStackCopy.clear()
        val stackSize: Int = fragmentManager.backStackEntryCount
        for (i in 0 until stackSize) {
            localStackCopy.add(fragmentManager.getBackStackEntryAt(i).name.orEmpty())
        }
    }

    /**
     * Perform transition described by the navigation command
     *
     * @param command the navigation command to apply
     */
    protected fun applyCommand(command: Command) {
        when (command) {
            is Forward -> {
                activityForward(command as Forward)
            }
            is Replace -> {
                activityReplace(command)
            }
            is BackTo -> {
                backTo(command)
            }
            is Back -> {
                fragmentBack()
            }
        }
    }

    protected fun activityForward(command: Forward) {
        val screen = command.screen as SupportAppScreen
        val activityIntent: Intent? = screen.getActivityIntent(activity)

        // Start activity
        if (activityIntent != null) {
            val options: Bundle? = createStartActivityOptions(command, activityIntent)
            checkAndStartActivity(screen, activityIntent, options)
        } else {
            fragmentForward(command)
        }
    }

    protected fun fragmentForward(command: Forward) {
        val screen = command.screen as SupportAppScreen
        val fragment: Fragment? = createFragment(screen)
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        setupFragmentTransaction(
            command,
            fragmentManager.findFragmentById(containerId),
            fragment,
            fragmentTransaction
        )
        fragment?.let {
            fragmentTransaction
                .replace(containerId, it)
                .addToBackStack(screen.screenKey)
                .commit()
        }
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
        val activityIntent: Intent? = screen.getActivityIntent(activity)

        // Replace activity
        if (activityIntent != null) {
            val options: Bundle? = createStartActivityOptions(command, activityIntent)
            checkAndStartActivity(screen, activityIntent, options)
            activity.finish()
        } else {
            fragmentReplace(command)
        }
    }

    protected fun fragmentReplace(command: Replace) {
        val screen = command.screen as SupportAppScreen
        val fragment: Fragment? = createFragment(screen)
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
            fragment?.let {
                fragmentTransaction
                    .replace(containerId, it)
                    .addToBackStack(screen.screenKey)
                    .commit()
            }
            localStackCopy.add(screen.screenKey)
        } else {
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            setupFragmentTransaction(
                command,
                fragmentManager.findFragmentById(containerId),
                fragment,
                fragmentTransaction
            )
            fragment?.let {
                fragmentTransaction
                    .replace(containerId, it)
                    .commit()
            }
        }
    }

    /**
     * Performs [BackTo] command transition
     */
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
                backToUnexisting(command.screen as SupportAppScreen)
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

    /**
     * Override this method to create option for start activity
     *
     * @param command        current navigation command. Will be only [Forward] or [Replace]
     * @param activityIntent activity intent
     * @return transition options
     */
    protected fun createStartActivityOptions(command: Command?, activityIntent: Intent?): Bundle? {
        return null
    }

    private fun checkAndStartActivity(
        screen: SupportAppScreen,
        activityIntent: Intent?,
        options: Bundle?
    ) {
        // Check if we can start activity
        if (activityIntent?.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(activityIntent, options)
        } else {
            unexistingActivity(screen, activityIntent)
        }
    }

    /**
     * Called when there is no activity to open `screenKey`.
     *
     * @param screen         screen
     * @param activityIntent intent passed to start Activity for the `screenKey`
     */
    protected fun unexistingActivity(screen: SupportAppScreen?, activityIntent: Intent?) {
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

    /**
     * Creates Fragment matching `screenKey`.
     *
     * @param screen screen
     * @return instantiated fragment for the passed screen
     */
    protected fun createFragment(screen: SupportAppScreen): Fragment? {
        val fragment: Fragment? = screen.getFragment()
        if (fragment == null) {
            errorWhileCreatingScreen(screen)
        }
        return fragment
    }

    /**
     * Called when we tried to fragmentBack to some specific screen (via [BackTo] command),
     * but didn't found it.
     *
     * @param screen screen
     */
    protected fun backToUnexisting(screen: SupportAppScreen?) {
        backToRoot()
    }

    protected fun errorWhileCreatingScreen(screen: SupportAppScreen) {
        throw RuntimeException("Can't create a screen: " + screen.screenKey)
    }

    init {
        this.activity = activity
        this.fragmentManager = fragmentManager
        this.containerId = containerId
    }
}
